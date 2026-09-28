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
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
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
    private var server: PicoServer? = null
    private val client = PicoClient()

    private val _shareUrl = MutableStateFlow<String?>(null)
    val shareUrl = _shareUrl.asStateFlow()

    private val _entries = MutableStateFlow<List<FileEntry>>(emptyList())
    val entries = _entries.asStateFlow()

    private val _shareError = MutableStateFlow<String?>(null)
    val shareError = _shareError.asStateFlow()

    private var receiveBase: String? = null
    private val _session = MutableStateFlow<SessionInfo?>(null)
    val session = _session.asStateFlow()

    private val _sessionError = MutableStateFlow<String?>(null)
    val sessionError = _sessionError.asStateFlow()

    private val _progress = MutableStateFlow<Map<String, Long>>(emptyMap())
    val progress = _progress.asStateFlow()

    private val _results = MutableStateFlow<List<RowResult>>(emptyList())
    val results = _results.asStateFlow()

    private val _busy = MutableStateFlow(false)
    val busy = _busy.asStateFlow()

    fun startSharing(source: FileSource) {
        stopSharingInternal()
        _entries.value = source.entries
        viewModelScope.launch {
            val attempts = if (DEFAULT_PORT != 0) listOf(DEFAULT_PORT, 0) else listOf(0)
            for (port in attempts) {
                val candidate = PicoServer(source, port = port)
                try {
                    candidate.start()
                    server = candidate
                    val ip = lanIpv4()
                    if (ip == null) {
                        _shareError.value = "No LAN address found. Check Wi-Fi."
                    } else {
                        _shareUrl.value = "http://$ip:${candidate.boundPort}"
                    }
                    return@launch
                } catch (e: Exception) {
                    PicoLog.w(logger, "binding port $port failed: ${e.message}")
                    candidate.stop()
                }
            }
            _shareError.value = "Could not bind any port. Stop blocking apps and retry."
        }
    }

    fun stopSharing() {
        stopSharingInternal()
        _shareUrl.value = null
        _shareError.value = null
    }

    private fun stopSharingInternal() {
        server?.let {
            it.stop()
            server = null
        }
    }

    fun connectSession(baseUrl: String) {
        val url = baseUrl.trim().trimEnd('/')
        if (url.isEmpty()) return
        viewModelScope.launch {
            _busy.value = true
            _sessionError.value = null
            try {
                receiveBase = url
                _session.value = client.fetchSession(url)
            } catch (e: Exception) {
                _sessionError.value = e.message ?: "connection failed"
            }
            _busy.value = false
        }
    }

    fun receiveAll() {
        val base = receiveBase ?: return
        val session = _session.value ?: return
        if (_busy.value) return
        val dir = File(getApplication<Application>().getExternalFilesDir(null), "PicoBeam")
        dir.mkdirs()
        viewModelScope.launch {
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
    }

    override fun onCleared() {
        server?.stop()
        client.close()
        super.onCleared()
    }
}