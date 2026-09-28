package com.picobeam.transfer

import com.picobeam.core.PicoLog
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.cio.CIO
import io.ktor.server.engine.ApplicationEngineFactory
import io.ktor.server.engine.EmbeddedServer
import io.ktor.server.engine.embeddedServer
import io.ktor.server.response.respondOutputStream
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import java.io.InputStream
import kotlin.math.min

/**
 * Embedded LAN server: exposes the transfer session as JSON ([SessionInfo]) for
 * the Android client, streams files with HTTP Range support for resume, and
 * serves a small browser page (Pico Web-Share).
 */
class PicoServer(
    private val source: FileSource,
    private val port: Int = DEFAULT_PORT,
) {
    private val logger: String = "PicoServer"
    private var engine: EmbeddedServer<*, *>? = null
    private val serverScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Volatile
    var boundPort: Int = 0
        private set

    /** Starts the server and returns immediately. Call [stop] to release the port. */
    suspend fun start(): Unit {
        check(engine == null) { "PicoServer already started" }
        val listenPort = port
        val factory: ApplicationEngineFactory<*, *> = CIO
        val server = serverScope.embeddedServer(factory, listenPort, "0.0.0.0") {
            routing {
                get("/") {
                    call.respondText(
                        text = WebPages.index(source),
                        contentType = ContentType.parse("text/html; charset=utf-8"),
                        status = HttpStatusCode.OK,
                    )
                }
                get("/session") {
                    val session = SessionInfo(
                        id = source.id,
                        device = source.deviceName,
                        version = ENGINE_VERSION,
                        files = source.entries,
                    )
                    PicoLog.d(logger, "GET /session -> ${session.files.size} files")
                    call.respondText(
                        text = WebPages.json(session),
                        contentType = ContentType.parse("application/json; charset=utf-8"),
                        status = HttpStatusCode.OK,
                    )
                }
                get("/file") {
                    val id = call.request.queryParameters["id"]
                        ?: return@get call.respondText(text = "missing id", status = HttpStatusCode.BadRequest)
                    val entry = source.entries.find { it.id == id }
                        ?: return@get call.respondText(text = "file not found", status = HttpStatusCode.NotFound)
                    val size = entry.size
                    val slice = Ranges.parse(call.request.headers[HttpHeaders.Range], size)
                    val partial = slice.start > 0 || slice.end < size - 1
                    val status = if (partial) HttpStatusCode.PartialContent else HttpStatusCode.OK
                    val mime = entry.mime.ifEmpty { "application/octet-stream" }
                    val safeName = entry.name.replace(Regex("[\\\\\"\r\n]"), "_")
                    call.response.headers.append(HttpHeaders.AcceptRanges, "bytes")
                    call.response.headers.append(
                        HttpHeaders.ContentDisposition,
                        "attachment; filename=\"$safeName\"",
                    )
                    if (partial) {
                        call.response.headers.append(
                            HttpHeaders.ContentRange,
                            "bytes ${slice.start}-${slice.end}/$size",
                        )
                    }
                    PicoLog.d(logger, "GET /file id=$id range=${slice.start}-${slice.end}/$size partial=$partial")
                    if (size < 0) {
                        call.respondOutputStream(
                            contentType = ContentType.parse(mime),
                            status = HttpStatusCode.OK,
                        ) {
                            copyRange(source.open(id), 0L, Long.MAX_VALUE, this)
                        }
                    } else {
                        call.respondOutputStream(
                            contentType = ContentType.parse(mime),
                            status = status,
                            contentLength = slice.end - slice.start + 1,
                        ) {
                            copyRange(source.open(id), slice.start, slice.end, this)
                        }
                    }
                }
            }
        }
        server.start(wait = false)
        engine = server
        boundPort = server.engine.resolvedConnectors().first().port
        PicoLog.i(logger, "server listening on port $boundPort")
    }

    fun stop() {
        engine?.let {
            it.stop(200L, 2000L)
            engine = null
            boundPort = 0
            PicoLog.i(logger, "server stopped")
        }
        serverScope.cancel()
    }

    private fun copyRange(input: InputStream, start: Long, end: Long, out: java.io.OutputStream) {
        input.use { stream ->
            var toSkip = start
            val skipBuf = ByteArray(8192)
            while (toSkip > 0) {
                val n = stream.read(skipBuf, 0, min(toSkip.toDouble(), skipBuf.size.toDouble()).toInt())
                if (n < 0) return
                toSkip -= n
            }
            val open = end == Long.MAX_VALUE
            val buf = ByteArray(64 * 1024)
            var left = if (open) 1L else end - start + 1
            while (left > 0) {
                val want = if (open || left > buf.size) buf.size else left.toInt()
                val n = stream.read(buf, 0, want)
                if (n < 0) return
                out.write(buf, 0, n)
                if (!open) left -= n
            }
        }
    }
}

internal object Ranges {
    data class Slice(val start: Long, val end: Long, val total: Long)

    private val RANGE = Regex("bytes=(\\d*)-(\\d*)")

    fun parse(header: String?, size: Long): Slice {
        if (header == null || size <= 0) return Slice(0, size - 1, size)
        val m = RANGE.find(header)
            ?: return Slice(0, size - 1, size)
        val startSpec = m.groupValues[1]
        val endSpec = m.groupValues[2]
        val maxEnd = (size - 1).coerceAtLeast(0)
        if (startSpec.isEmpty()) {
            if (endSpec.isEmpty()) return Slice(0, maxEnd, size)
            val n = endSpec.toLongOrNull()
                ?: return Slice(0, maxEnd, size)
            val start = (size - n).coerceIn(0, maxEnd)
            return Slice(start, maxEnd, size)
        }
        val start = (startSpec.toLongOrNull() ?: 0).coerceIn(0, maxEnd)
        val end = if (endSpec.isEmpty()) maxEnd else (endSpec.toLongOrNull() ?: maxEnd).coerceIn(start, maxEnd)
        return Slice(start, end, size)
    }
}