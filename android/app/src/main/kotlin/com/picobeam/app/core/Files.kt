package com.picobeam.app.core

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.os.Build
import com.picobeam.transfer.FileEntry
import com.picobeam.transfer.FileSource
import java.io.InputStream

/**
 * [FileSource] backed by the ContentResolver. Document URIs are opened each
 * time a client asks for a slice, so concurrent downloads each get their own
 * stream.
 */
class ContentResolverFileSource(
    private val resolver: ContentResolver,
    private val docs: List<Doc>,
) : FileSource {

    data class Doc(
        val id: String,
        val name: String,
        val size: Long,
        val mime: String,
        val uri: Uri,
    )

    override val id: String = "pb-${System.currentTimeMillis().toString(36)}"
    override val deviceName: String = Build.MODEL ?: "Android"
    override val entries: List<FileEntry> =
        docs.map { FileEntry(id = it.id, name = it.name, size = it.size, mime = it.mime) }

    override fun open(id: String): InputStream {
        val doc = docs.firstOrNull { it.id == id } ?: error("unknown file id: $id")
        return requireNotNull(resolver.openInputStream(doc.uri)) { "cannot open ${doc.name}" }
    }

    companion object {
        fun resolve(context: Context, uris: List<Uri>): List<Doc> = uris.map { uri ->
            val cr = context.contentResolver
            var name = "file"
            var size = -1L
            cr.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE), null, null, null)?.use { c ->
                if (c.moveToFirst()) {
                    if (!c.isNull(0)) name = c.getString(0)
                    if (!c.isNull(1)) size = c.getLong(1)
                }
            }
            if (size < 0) {
                cr.openFileDescriptor(uri, "r")?.use { fd -> size = fd.statSize }
            }
            Doc(
                id = uri.toString(),
                name = name,
                size = size,
                mime = cr.getType(uri).orEmpty(),
                uri = uri,
            )
        }
    }
}