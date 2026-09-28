/*
 * PicoBeam — peer-to-peer file transfer for Android
 * Copyright (C) 2026 Hassan (a.k.a. EuroMoscow)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.picobeam.transfer

import kotlinx.serialization.Serializable

/** Version advertised inside a transfer session. Keep in sync with the app versionName. */
const val ENGINE_VERSION = "1.0.0"

/** Listen port for the embedded server (IPv4). */
const val DEFAULT_PORT = 8765

/** How many files may download concurrently through [PicoClient.downloadFiles]. */
const val DEFAULT_PARALLELISM = 3

/** Byte range window (1 MiB) for single-file resume chunks. */
const val DEFAULT_CHUNK_SIZE = 1L shl 20

@Serializable
data class FileEntry(
    val id: String,
    val name: String,
    val size: Long,
    val mime: String,
)

@Serializable
data class SessionInfo(
    val id: String,
    val device: String,
    val version: String = ENGINE_VERSION,
    val files: List<FileEntry> = emptyList(),
)

/**
 * Contract every transfer source must implement. Android wires this to a
 * ContentResolver; tests wire it to byte arrays.
 */
interface FileSource {
    val id: String
    val deviceName: String
    val entries: List<FileEntry>
    fun open(id: String): java.io.InputStream
}

enum class TransferStatus { DOWNLOADING, DONE, FAILED, CANCELLED }

data class TransferResult(
    val file: FileEntry,
    val destinationPath: String,
    val byteCount: Long,
    val status: TransferStatus,
    val error: String? = null,
)