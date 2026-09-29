/*
 * PicoBeam — peer-to-peer file transfer for Android
 * Copyright (C) 2026 Hassan (a.k.a. EuroMoscow)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.picobeam.app

import android.app.Application
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.picobeam.app.core.HotspotInfo
import com.picobeam.app.core.HotspotJoiner
import com.picobeam.app.core.LocalHotspot
import com.picobeam.app.core.SharePayload
import com.picobeam.app.core.ThemeMode
import com.picobeam.app.core.appSettings
import com.picobeam.app.core.candidateUrls
import com.picobeam.app.core.lanIpv4
import com.picobeam.core.PicoLog
import com.picobeam.transfer.DEFAULT_PARALLELISM
import com.picobeam.transfer.DEFAULT_PORT
import com.picobeam.transfer.FileEntry
import com.picobeam.transfer.FileSource
import com.picobeam.transfer.PicoClient
import com.picobeam.transfer.PicoServer
import com.picobeam.transfer.SessionInfo
import com.picobeam.transfer.TransferResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

data class RowResult(
    val name: String,
    val status: String,
    val bytes: Long,
    val error: String? = null,
)

class HubViewModel(app: Application) : AndroidViewModel(app) {

    private val logger = "Hub"
    private val settings = appSettings(app)
    private var server: PicoServer? = null
    private val client = PicoClient()
    private var hotspot: LocalHotspot? = null

    val themeMode = settings.themeMode
    val lang = settings.lang

    private val _shareUrl = MutableStateFlow<String?>(null)
    val shareUrl = _shareUrl.asStateFlow()

    private val _entries = MutableStateFlow<List<FileEntry>>(emptyList())
    val entries = _entries.asStateFlow()

    private val _shareError = MutableStateFlow<String?>(null)
    val shareError = _shareError.asStateFlow()

    private val _hotspotInfo = MutableStateFlow<HotspotInfo?>(null)
    val hotspotInfo = _hotspotInfo.asStateFlow()

    private val _payload = MutableStateFlow<String?>(null)
    val payload = _payload.asStateFlow()

    private var receiveBase: String? = null
    private val _session = MutableStateFlow<SessionInfo?>(null)
    val session = _session.asStateFlow()

    private val _sessionError = MutableStateFlow<String?>(null)
    val sessionError = _sessionError.asStateFlow()

    private val _joinState = MutableStateFlow<String?>(null)
    val joinState = _joinState.asStateFlow()

    private val _manualJoin = MutableStateFlow<SharePayload?>(null)
    val manualJoin = _manualJoin.asStateFlow()

    private val _progress = MutableStateFlow<Map<String, Long>>(emptyMap())
    val progress = _progress.asStateFlow()

    private val _results = MutableStateFlow<List<RowResult>>(emptyList())
    val results = _results.asStateFlow()

    private val _busy = MutableStateFlow(false)
    val busy = _busy.asStateFlow()

    // ---- settings -----------------------------------------------------------

    fun setThemeMode(mode: ThemeMode) = settings.setThemeMode(mode)
    fun setLang(code: String) = settings.setLang(code)

    // ---- send ---------------------------------------------------------------

    private var docs = mutableListOf<com.picobeam.app.core.ContentResolverFileSource.Doc>()

    /** Resolves + persists picked URIs and keeps them ready for sharing. */
    fun addFiles(uris: List<android.net.Uri>) {
        if (uris.isEmpty()) return
        val context = getApplication<Application>()
        viewModelScope.launch(Dispatchers.IO) {
            val resolved = com.picobeam.app.core.ContentResolverFileSource.resolve(context, uris)
            uris.forEach { uri ->
                runCatching {
                    context.contentResolver.takePersistableUriPermission(
                        uri,
                        android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION,
                    )
                }
            }
            docs.addAll(resolved)
            refreshEntries()
        }
    }

    fun removeFile(id: String) {
        docs.removeAll { it.id == id }
        refreshEntries()
    }

    fun clearFiles() {
        docs.clear()
        refreshEntries()
    }

    private fun refreshEntries() {
        _entries.value = docs.map {
            FileEntry(id = it.id, name = it.name, size = it.size, mime = it.mime)
        }
    }

    fun startSharing() {
        if (docs.isEmpty()) return
        startSharing(
            com.picobeam.app.core.ContentResolverFileSource(
                getApplication<Application>().contentResolver,
                docs.toList(),
            ),
        )
    }

    private fun startSharing(source: FileSource) {
        stopSharingInternal()
        refreshEntries()
        viewModelScope.launch(Dispatchers.IO) {
            val attempts = if (DEFAULT_PORT != 0) listOf(DEFAULT_PORT, 0) else listOf(0)
            for (port in attempts) {
                val candidate = PicoServer(source, port = port)
                try {
                    candidate.start()
                    server = candidate
                    launchHotspot()
                    val ip = lanIpv4()
                    if (ip == null) {
                        _shareError.value = null
                        _shareUrl.value = "http://192.168.43.1:${candidate.boundPort}"
                    } else {
                        _shareUrl.value = "http://$ip:${candidate.boundPort}"
                    }
                    publishPayload()
                    return@launch
                } catch (e: Exception) {
                    PicoLog.w(logger, "binding port $port failed: ${e.message}")
                    candidate.stop()
                }
            }
            _shareError.value = "Could not bind any port. Stop blocking apps and retry."
        }
    }

    private fun launchHotspot() {
        val self = LocalHotspot(getApplication())
        hotspot = self
        viewModelScope.launch(Dispatchers.IO) {
            val info = runCatching { self.start() }.getOrNull()
            _hotspotInfo.value = info
            publishPayload()
        }
    }

    private fun publishPayload() {
        val base = _shareUrl.value ?: return
        val hp = _hotspotInfo.value
        _payload.value = SharePayload(u = base, s = hp?.ssid?.trim('"'), p = hp?.passphrase).encode()
    }

    fun stopSharing() {
        stopSharingInternal()
        hotspot?.stop()
        hotspot = null
        _hotspotInfo.value = null
        _payload.value = null
        _shareUrl.value = null
        _shareError.value = null
    }

    private fun stopSharingInternal() {
        server?.let {
            it.stop()
            server = null
        }
    }

    // ---- receive ------------------------------------------------------------

    fun connectSession(baseUrl: String) {
        val url = baseUrl.trim().trimEnd('/')
        if (url.isEmpty()) return
        viewModelScope.launch(Dispatchers.IO) {
            connectCandidates(url)
        }
    }

    /** Entry point for a scanned QR code: JSON payload or plain URL. */
    fun connectFromScan(raw: String) {
        val p = SharePayload.fromScan(raw)
        if (p == null || p.u.isBlank()) {
            if (raw.trim().startsWith("http")) connectSession(raw.trim())
            else _sessionError.value = "Unrecognized QR code."
            return
        }
        if (p.s.isNullOrEmpty()) {
            connectSession(p.u)
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            viewModelScope.launch(Dispatchers.IO) {
                _joinState.value = "joining"
                val ok = HotspotJoiner(getApplication()).join(p.s, p.p)
                _joinState.value = null
                if (ok) connectCandidates(p.u)
                else _sessionError.value = "Could not join the sender hotspot."
            }
        } else {
            _manualJoin.value = p
        }
    }

    fun dismissManualJoin() {
        _manualJoin.value = null
    }

    /** User manually joined the hotspot (Android < 10 path), now connect. */
    fun acceptManualJoin() {
        val p = _manualJoin.value ?: return
        _manualJoin.value = null
        connectSession(p.u)
    }

    private suspend fun connectCandidates(baseUrl: String) {
        _busy.value = true
        _sessionError.value = null
        val errors = mutableListOf<String>()
        try {
            for (candidate in candidateUrls(baseUrl)) {
                try {
                    receiveBase = candidate
                    _session.value = client.fetchSession(candidate)
                    return
                } catch (e: Exception) {
                    errors += e.message ?: "connection failed"
                }
            }
            _sessionError.value = errors.lastOrNull() ?: "connection failed"
        } finally {
            _busy.value = false
        }
    }

    fun receiveAll() {
        val base = receiveBase ?: return
        val session = _session.value ?: return
        if (_busy.value) return
        val dir = File(getApplication<Application>().getExternalFilesDir(null), "PicoBeam")
        dir.mkdirs()
        viewModelScope.launch(Dispatchers.IO) {
            _busy.value = true
            _results.value = emptyList()
            _progress.value = emptyMap()
            val list: List<TransferResult> = try {
                client.downloadFiles(
                    baseUrl = base,
                    entries = session.files,
                    destinationDir = dir,
                    parallelism = DEFAULT_PARALLELISM,
                    onProgress = { map -> _progress.value = map },
                )
            } catch (e: Exception) {
                PicoLog.e(logger, "receive failed: ${e.message}")
                emptyList()
            }
            _results.value = list.map {
                RowResult(
                    name = it.file.name,
                    status = it.status.name,
                    bytes = it.byteCount,
                    error = it.error,
                )
            }
            _busy.value = false
        }
    }

    fun clearSession() {
        _session.value = null
        _results.value = emptyList()
        _progress.value = emptyMap()
        receiveBase = null
        _sessionError.value = null
    }

    override fun onCleared() {
        server?.stop()
        hotspot?.stop()
        client.close()
        super.onCleared()
    }
}