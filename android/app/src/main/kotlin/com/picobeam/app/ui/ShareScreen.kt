package com.picobeam.app.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.picobeam.app.HubViewModel
import com.picobeam.app.R
import com.picobeam.app.core.ContentResolverFileSource
import com.picobeam.app.core.qrBitmap
import com.picobeam.transfer.FileEntry
import kotlin.math.roundToInt

@Composable
fun ShareScreen(vm: HubViewModel) {
    val url by vm.shareUrl.collectAsState()
    val entries by vm.entries.collectAsState()
    val error by vm.shareError.collectAsState()
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current

    val picker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenMultipleDocuments(),
    ) { uris ->
        if (uris.isEmpty()) return@rememberLauncherForActivityResult
        val docs = ContentResolverFileSource.resolve(context, uris)
        uris.forEach { uri ->
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION,
                )
            }
        }
        vm.startSharing(ContentResolverFileSource(context.contentResolver, docs))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
    ) {
        Text("PicoBeam", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(
            "Send files to another device over your local network. Nothing leaves your Wi-Fi, nothing hits the cloud.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp),
        )

        Spacer(Modifier.height(20.dp))

        if (url == null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            ) {
                Column(Modifier.padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Choose files to share", fontWeight = FontWeight.SemiBold)
                    Text(
                        "Images, videos, APKs, docs… anything. The receiver or a browser can grab them.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                    Spacer(Modifier.height(14.dp))
                    Button(onClick = { picker.launch(arrayOf("*/*")) }) {
                        Text(remember { context.getString(R.string.add_files) })
                    }
                }
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Box(
                            Modifier
                                .size(10.dp)
                                .background(MaterialTheme.colorScheme.primary),
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Sharing now", fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.weight(1f))
                        IconButton(onClick = { vm.stopSharing() }) {
                            Icon(Icons.Filled.Close, contentDescription = context.getString(R.string.stop_sharing))
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    val qr = remember(url) { url?.let { qrBitmap(it) } }
                    if (qr != null) {
                        Image(
                            bitmap = qr,
                            contentDescription = url,
                            modifier = Modifier
                                .height(220.dp)
                                .width(220.dp),
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    Text(
                        url ?: "",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        "Scan the QR or open this address on a device on the same Wi-Fi. Browsers can download without installing anything.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 6.dp),
                    )

                    Spacer(Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = {
                            url?.let { clipboard.setText(AnnotatedString(it)) }
                        },
                        enabled = url != null,
                    ) {
                        Text("Copy link")
                    }

                    Spacer(Modifier.height(4.dp))

                    Text(
                        "${entries.size} file(s) ready",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(entries) { entry -> FileRow(entry) }
            }
        }

        if (error != null) {
            Spacer(Modifier.height(14.dp))
            Text(
                error ?: "",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun FileRow(entry: FileEntry) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                entry.name,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.width(10.dp))
            Text(
                humanSize(entry.size),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

internal fun humanSize(bytes: Long): String {
    if (bytes < 1024) return "$bytes B"
    val units = arrayOf("KB", "MB", "GB", "TB")
    var value = bytes.toDouble()
    var unit = -1
    do {
        value /= 1024.0
        unit++
    } while (value >= 1024 && unit < units.size - 1)
    return "${(value * 10).roundToInt() / 10.0} ${units[unit]}"
}