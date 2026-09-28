package com.picobeam.transfer

import com.picobeam.core.PicoLog
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsBytes
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.File
import java.io.InputStream
import java.nio.file.Files

class TransferEngineTest {

    private val tmp = Files.createTempDirectory("picobeam-test").toFile()
    private lateinit var source: MemorySource
    private lateinit var server: PicoServer
    private lateinit var client: PicoClient

    private class MemorySource(
        val store: Map<String, ByteArray>,
        override val id: String = "test-session",
        override val deviceName: String = "CI Runner",
    ) : FileSource {
        override val entries: List<FileEntry> = store.map { (name, data) ->
            FileEntry(id = name, name = name, size = data.size.toLong(), mime = mimeOf(name))
        }

        override fun open(id: String): InputStream = ByteArrayInputStream(store.getValue(id))

        fun bytesOf(name: String): ByteArray = store.getValue(name)

        private fun mimeOf(name: String): String = when (name.substringAfterLast('.')) {
            "png", "jpg", "jpeg" -> "image/$name.substringAfterLast('.')"
            else -> "application/octet-stream"
        }
    }

    companion object {
        private const val MB = 1024 * 1024L

        fun randomBytes(size: Int): ByteArray {
            val out = ByteArray(size)
            var seed = 42L
            for (i in out.indices) {
                seed = seed * 6364136223846793005L + 1442695040888963407L
                out[i] = (seed ushr 32).toByte()
            }
            return out
        }
    }

    @Before
    fun setUp() = runTest {
        PicoLog.configure {}
        source = MemorySource(
            mapOf(
                "big.bin" to randomBytes((6 * MB).toInt()),
                "medium.bin" to randomBytes((3 * MB).toInt()),
                "small.bin" to randomBytes((1 * MB).toInt()),
            ),
        )
        server = PicoServer(source, port = 0)
        server.start()
        client = PicoClient()
    }

    @After
    fun tearDown() {
        server.stop()
        client.close()
        tmp.deleteRecursively()
    }

    private fun baseUrl(): String = "http://127.0.0.1:${server.boundPort}"

    @Test
    fun `web share page serves PicoBeam html`() = runTest {
        HttpClient(CIO).use { http ->
            val page = http.get("${baseUrl()}/").bodyAsText()
            assertTrue("web page should contain PicoBeam", page.contains("PicoBeam"))
            assertTrue("web page should contain device name", page.contains("CI Runner"))
            assertTrue("web page should show file count", page.contains("3 file(s) ready"))
            assertTrue("web page should link to file endpoint", page.contains("./file?id="))
        }
    }

    @Test
    fun `session json lists all files`() = runTest {
        val session = client.fetchSession(baseUrl())
        assertEquals("test-session", session.id)
        assertEquals(3, session.files.size)
        assertEquals(6 * MB, session.files.first { it.name == "big.bin" }.size)
        assertEquals(ENGINE_VERSION, session.version)
    }

    @Test
    fun `full download produces byte-identical file`() = runTest {
        val entry = source.entries.first { it.name == "big.bin" }
        val dest = File(tmp, "out/big.bin")
        val result = client.downloadFile(baseUrl(), entry, dest)
        assertEquals(TransferStatus.DONE, result.status)
        assertEquals(6 * MB, result.byteCount)
        assertArrayEquals(
            "streamed bytes must match source",
            source.bytesOf("big.bin"),
            dest.readBytes(),
        )
    }

    @Test
    fun `interrupted download resumes without restart`() = runTest {
        val entry = source.entries.first { it.name == "big.bin" }
        val dest = File(tmp, "resume/big.bin")
        val full = source.bytesOf("big.bin")
        dest.parentFile?.mkdirs()
        dest.writeBytes(full.copyOfRange(0, (1 * MB).toInt()))
        assertEquals(1 * MB, dest.length())

        val result = client.downloadFile(baseUrl(), entry, dest)

        assertEquals(TransferStatus.DONE, result.status)
        assertArrayEquals("resumed file must equal source", full, dest.readBytes())
    }

    @Test
    fun `range request returns exact 206 slice`() = runTest {
        val entry = source.entries.first { it.name == "big.bin" }
        HttpClient(CIO).use { http ->
            val response = http.get("${baseUrl()}/file?id=${entry.id}") {
                header(HttpHeaders.Range, "bytes=${1 * MB}-${2 * MB - 1}")
            }
            assertEquals(HttpStatusCode.PartialContent, response.status)
            assertEquals(
                "bytes ${1 * MB}-${2 * MB - 1}/${6 * MB}",
                response.headers[HttpHeaders.ContentRange],
            )
            val slice = response.bodyAsBytes()
            val expected = source.bytesOf("big.bin").copyOfRange((1 * MB).toInt(), (2 * MB).toInt())
            assertArrayEquals(expected, slice)
        }
    }

    @Test
    fun `suffix range bytes=-N returns last N bytes`() = runTest {
        val entry = source.entries.first { it.name == "small.bin" }
        HttpClient(CIO).use { http ->
            val response = http.get("${baseUrl()}/file?id=${entry.id}") {
                header(HttpHeaders.Range, "bytes=-1000")
            }
            assertEquals(HttpStatusCode.PartialContent, response.status)
            val slice = response.bodyAsBytes()
            assertEquals(1000, slice.size)
            val expected = source.bytesOf("small.bin").copyOfRange(
                source.bytesOf("small.bin").size - 1000,
                source.bytesOf("small.bin").size,
            )
            assertArrayEquals(expected, slice)
        }
    }

    @Test
    fun `parallel downloads of three files all complete`() = runTest {
        val destDir = File(tmp, "parallel")
        val results = client.downloadFiles(baseUrl(), source.entries, destDir)
        assertEquals(3, results.size)
        assertTrue("all transfers must be DONE", results.all { it.status == TransferStatus.DONE })
        assertEquals(10 * MB, results.sumOf { it.byteCount })
        for (entry in source.entries) {
            assertArrayEquals(
                "'${entry.name}' must match source",
                source.bytesOf(entry.name),
                File(destDir, entry.name).readBytes(),
            )
        }
    }

    @Test
    fun `missing file returns not found`() = runTest {
        HttpClient(CIO).use { http ->
            val response = http.get("${baseUrl()}/file?id=nope")
            assertEquals(HttpStatusCode.NotFound, response.status)
        }
    }
}