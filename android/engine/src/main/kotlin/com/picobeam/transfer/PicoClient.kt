package com.picobeam.transfer

import com.picobeam.core.PicoLog
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.supervisorScope
import kotlinx.serialization.json.Json
import java.io.File
import java.io.RandomAccessFile
import java.util.concurrent.atomic.AtomicLong

/**
 * Resumable download engine. Requests explicit HTTP Range windows so an
 * interrupted transfer continues where it stopped instead of restarting.
 */
class PicoClient {

    private val logger: String = "PicoClient"

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json { encodeDefaults = true })
        }
    }

    suspend fun fetchSession(baseUrl: String): SessionInfo = client.get("$baseUrl/session").body()

    suspend fun downloadFile(
        baseUrl: String,
        entry: FileEntry,
        destination: File,
        onProgress: suspend (written: Long) -> Unit = {},
    ): TransferResult {
        val existing = if (destination.exists()) destination.length() else 0L
        if (existing >= entry.size && entry.size > 0) {
            PicoLog.i(logger, "'${entry.name}' already complete, skipping")
            return TransferResult(entry, destination.absolutePath, existing, TransferStatus.DONE)
        }
        var start = existing.coerceIn(0, entry.size - 1)
        return try {
            PicoLog.i(logger, "'${entry.name}' start=$start size=${entry.size}")
            val response = client.get("$baseUrl/file?id=${entry.id}") {
                if (start > 0) header(HttpHeaders.Range, "bytes=$start-")
            }
            when (response.status) {
                HttpStatusCode.PartialContent, HttpStatusCode.OK -> {
                    if (response.status == HttpStatusCode.OK && start > 0) {
                        PicoLog.w(logger, "server ignored Range; restarting '${entry.name}' from 0")
                        start = 0L
                    }
                    destination.parentFile?.mkdirs()
                    val src: kotlinx.io.Source = response.body()
                    val written = AtomicLong(start)
                    RandomAccessFile(destination, "rw").use { raf ->
                        raf.setLength(start)
                        raf.seek(start)
                        drainSource(src, raf, written, onProgress)
                    }
                    val bytes = written.get()
                    if (bytes == entry.size) {
                        PicoLog.i(logger, "'${entry.name}' complete ($bytes/${entry.size})")
                        TransferResult(entry, destination.absolutePath, bytes, TransferStatus.DONE)
                    } else {
                        PicoLog.w(logger, "'${entry.name}' short transfer bytes=$bytes")
                        TransferResult(entry, destination.absolutePath, bytes, TransferStatus.FAILED, "short transfer")
                    }
                }
                else -> {
                    PicoLog.e(logger, "'${entry.name}' unexpected status ${response.status}")
                    TransferResult(entry, destination.absolutePath, writtenSoFar(destination), TransferStatus.FAILED, "HTTP ${response.status.value}")
                }
            }
        } catch (e: CancellationException) {
            PicoLog.w(logger, "'${entry.name}' cancelled at ${writtenSoFar(destination)}")
            TransferResult(entry, destination.absolutePath, writtenSoFar(destination), TransferStatus.CANCELLED)
        } catch (e: Exception) {
            PicoLog.e(logger, "'${entry.name}' failed: ${e.message}")
            TransferResult(
                entry,
                destination.absolutePath,
                if (destination.exists()) destination.length() else 0L,
                TransferStatus.FAILED,
                e.message,
            )
        }
    }

    /** Downloads several files concurrently (up to [parallelism]) into [destinationDir]. */
    suspend fun downloadFiles(
        baseUrl: String,
        entries: List<FileEntry>,
        destinationDir: File,
        parallelism: Int = DEFAULT_PARALLELISM,
        onProgress: suspend (progress: Map<String, Long>) -> Unit = {},
    ): List<TransferResult> = supervisorScope {
        val rate = parallelism.coerceIn(1, 8)
        PicoLog.i(logger, "downloading ${entries.size} files, parallelism=$rate")
        val results = mutableListOf<TransferResult>()
        entries.chunked(rate).forEach { batch ->
            val batchResults = batch.map { entry ->
                val dest = File(destinationDir, sanitize(entry.name))
                async {
                    downloadFile(baseUrl, entry, dest) { written ->
                        onProgress(mapOf(entry.id to written))
                    }
                }
            }.map { it.await() }
            results += batchResults
        }
        results
    }

    private fun writtenSoFar(destination: File): Long =
        if (destination.exists()) destination.length() else 0L

    /** Streams [source] into [raf], suspending only when no bytes are pending. */
    private suspend fun drainSource(
        source: kotlinx.io.Source,
        raf: RandomAccessFile,
        written: AtomicLong,
        onProgress: suspend (Long) -> Unit,
    ) {
        val buffer = ByteArray(DEFAULT_CHUNK_SIZE.toInt())
        try {
            while (true) {
                val n = source.readAtMostTo(buffer, 0, buffer.size)
                if (n < 0) break
                if (n == 0) {
                    if (source.exhausted()) break
                    kotlinx.coroutines.yield()
                    continue
                }
                raf.write(buffer, 0, n)
                written.addAndGet(n.toLong())
                onProgress(written.get())
            }
        } catch (e: java.io.EOFException) {
            // natural end of the channel-backed source
        }
    }

    private fun sanitize(name: String): String =
        name.replace(Regex("[\\\\/:*?\"<>|]"), "_").let { if (it.isBlank()) "file" else it }

    fun close() {
        client.close()
        PicoLog.i(logger, "client closed")
    }
}