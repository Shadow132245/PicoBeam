/*
 * PicoBeam — peer-to-peer file transfer for Android
 * Copyright (C) 2026 Hassan (a.k.a. EuroMoscow)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.picobeam.app.ui

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.picobeam.app.HubViewModel
import com.picobeam.app.R
import com.picobeam.app.core.SharePayload
import com.picobeam.transfer.SessionInfo
import java.util.Locale

@Composable
fun ReceiveScreen(vm: HubViewModel) {
    val session by vm.session.collectAsState()
    val sessionError by vm.sessionError.collectAsState()
    val progress by vm.progress.collectAsState()
    val results by vm.results.collectAsState()
    val busy by vm.busy.collectAsState()
    val joinState by vm.joinState.collectAsState()
    val manualJoin by vm.manualJoin.collectAsState()
    val context = LocalContext.current
    var input by rememberSaveable { mutableStateOf("") }
    var scanning by rememberSaveable { mutableStateOf(false) }

    Box(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                context.getString(R.string.receive_header),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                context.getString(R.string.receive_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Button(
                onClick = { scanning = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
            ) {
                Icon(Icons.Filled.QrCode2, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                Text(context.getString(R.string.scan_qr_button), style = MaterialTheme.typography.titleMedium)
            }

            Text(
                context.getString(R.string.manual_url_fallback),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(top = 6.dp),
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    placeholder = { Text(context.getString(R.string.url_hint)) },
                    singleLine = true,
                    enabled = !busy,
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = { vm.connectSession(input) },
                    enabled = input.isNotBlank() && !busy,
                ) {
                    Text(context.getString(R.string.connect))
                }
            }

            if (joinState != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    CircularProgressIndicator(Modifier.height(18.dp).width(18.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(10.dp))
                    Text(
                        context.getString(R.string.joining_hotspot),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }

            val err = sessionError
            if (err != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(
                        Icons.Filled.ErrorOutline,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(end = 8.dp),
                    )
                    Text(
                        err,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }

            session?.let { s ->
                SessionCard(
                    session = s,
                    onDownloadAll = { vm.receiveAll() },
                    downloading = busy,
                )
                if (!busy && results.isEmpty()) {
                    OutlinedButton(onClick = { vm.clearSession() }, modifier = Modifier.fillMaxWidth()) {
                        Text(context.getString(R.string.clear))
                    }
                }
            }

            if (progress.isNotEmpty()) {
                Text(
                    context.getString(R.string.downloading),
                    style = MaterialTheme.typography.bodySmall,
                )
                progress.toList().forEach { (name, written) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            name,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                        )
                        Text(
                            humanSize(written),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            if (results.isNotEmpty()) {
                Text(
                    context.getString(R.string.results),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 6.dp),
                )
                results.forEach { r ->
                    val ok = r.status == "DONE"
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (ok) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.errorContainer,
                        ),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                if (ok) Icons.Filled.CheckCircle else Icons.Filled.ErrorOutline,
                                contentDescription = null,
                                tint = if (ok) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(end = 10.dp),
                            )
                            Text(
                                r.name,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                            )
                            Text(
                                if (ok) humanSize(r.bytes)
                                else r.status.lowercase(Locale.ROOT) + (r.error?.let { " · $it" } ?: ""),
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                }
                Text(
                    context.getString(R.string.files_saved),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                OutlinedButton(onClick = { vm.clearSession() }, modifier = Modifier.fillMaxWidth()) {
                    Text(context.getString(R.string.clear))
                }
            }
        }

        if (scanning) {
            ScannerOverlay(
                onScanned = { text ->
                    scanning = false
                    vm.connectFromScan(text)
                },
                onClose = { scanning = false },
            )
        }
    }

    manualJoin?.let { p ->
        ManualJoinDialog(
            payload = p,
            onContinue = vm::acceptManualJoin,
            onCancel = vm::dismissManualJoin,
        )
    }
}

@Composable
private fun ScannerOverlay(onScanned: (String) -> Unit, onClose: () -> Unit) {
    val context = LocalContext.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        QrScanner(onScanned = onScanned, modifier = Modifier.fillMaxSize())
        IconButton(
            onClick = onClose,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(12.dp)
                .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(20.dp)),
        ) {
            Icon(
                Icons.Filled.Close,
                contentDescription = context.getString(R.string.cancel),
                tint = Color.White,
            )
        }
        Text(
            context.getString(R.string.scanning),
            color = Color.White,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(24.dp),
        )
    }
}

@Composable
private fun ManualJoinDialog(
    payload: SharePayload,
    onContinue: () -> Unit,
    onCancel: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onCancel,
        icon = {
            Icon(Icons.Filled.CameraAlt, contentDescription = null)
        },
        title = { Text(stringResource(R.string.join_manually_title)) },
        text = {
            Text(stringResource(R.string.join_manually_body, payload.s ?: "", payload.p ?: ""))
        },
        confirmButton = {
            Button(onClick = onContinue) {
                Text(stringResource(R.string.manual_continue))
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text(stringResource(R.string.cancel))
            }
        },
    )
}

@Composable
private fun SessionCard(
    session: SessionInfo,
    onDownloadAll: () -> Unit,
    downloading: Boolean,
) {
    val context = LocalContext.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
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
                    "v${session.version} · ${context.getString(R.string.files_ready, session.files.size)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.height(10.dp))
            session.files.forEach { f ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        f.name,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    )
                    Text(
                        humanSize(f.size),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            if (downloading) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(Modifier.height(20.dp).width(20.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(10.dp))
                    Text(context.getString(R.string.transferring), style = MaterialTheme.typography.bodySmall)
                }
            } else {
                Button(onClick = onDownloadAll, modifier = Modifier.fillMaxWidth()) {
                    Text(context.getString(R.string.download_all))
                }
                Text(
                    context.getString(R.string.files_saved),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 6.dp),
                )
            }
        }
    }
}