package com.picobeam.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.picobeam.app.HubViewModel
import com.picobeam.app.R
import com.picobeam.transfer.SessionInfo
import java.util.Locale

@Composable
fun ReceiveScreen(vm: HubViewModel) {
    val session by vm.session.collectAsState()
    val sessionError by vm.sessionError.collectAsState()
    val progress by vm.progress.collectAsState()
    val results by vm.results.collectAsState()
    val busy by vm.busy.collectAsState()
    val context = LocalContext.current
    var input by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
    ) {
        Text("Receive", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(
            "Paste the sender's address from the QR or enter it manually.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp),
        )

        Spacer(Modifier.height(18.dp))

        OutlinedTextField(
            value = input,
            onValueChange = { input = it },
            label = { Text(context.getString(R.string.url_hint)) },
            placeholder = { Text("http://192.168.1.20:8765") },
            singleLine = true,
            enabled = !busy,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(10.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = { vm.connectSession(input) },
                enabled = input.isNotBlank() && !busy,
            ) {
                Text(context.getString(R.string.connect))
            }
            if (session != null) {
                OutlinedButton(onClick = { vm.clearSession() }, enabled = !busy) {
                    Text("Clear")
                }
            }
        }

        if (sessionError != null) {
            Spacer(Modifier.height(12.dp))
            Text(
                sessionError ?: "",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
            )
        }

        session?.let { s ->
            SessionCard(
                session = s,
                onDownloadAll = { vm.receiveAll() },
                downloading = busy,
            )
        }

        if (progress.isNotEmpty()) {
            Spacer(Modifier.height(14.dp))
            Text("Downloading…", style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(6.dp))
            progress.toList().forEach { (name, written) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(name, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                    Text(humanSize(written), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        if (results.isNotEmpty()) {
            Spacer(Modifier.height(18.dp))
            Text("Results", fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items(results) { r ->
                    val ok = r.status == "DONE"
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (ok) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.errorContainer,
                        ),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                r.name,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f),
                            )
                            Text(
                                if (ok) humanSize(r.bytes) else (r.status.lowercase(Locale.ROOT) + (r.error?.let { " · $it" } ?: "")),
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ColumnScope.SessionCard(
    session: SessionInfo,
    onDownloadAll: () -> Unit,
    downloading: Boolean,
) {
    Spacer(Modifier.height(16.dp))
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    session.device,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "v${session.version} · ${session.files.size} file(s)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.height(10.dp))
            session.files.forEach { f ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(f.name, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                    Text(humanSize(f.size), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(Modifier.height(12.dp))
            if (downloading) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(Modifier.height(20.dp).width(20.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(10.dp))
                    Text("Transferring…", style = MaterialTheme.typography.bodySmall)
                }
            } else {
                Button(onClick = onDownloadAll, modifier = Modifier.fillMaxWidth()) {
                    Text("Download all")
                }
                Text(
                    "Files land in PicoBeam under your app storage.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 6.dp),
                )
            }
        }
    }
}