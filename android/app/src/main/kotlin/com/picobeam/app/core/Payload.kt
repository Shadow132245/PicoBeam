/*
 * PicoBeam — peer-to-peer file transfer for Android
 * Copyright (C) 2026 Hassan (a.k.a. EuroMoscow)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.picobeam.app.core

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Payload encoded into the share QR:
 *  - u: the share URL (http://ip:port)
 *  - s/p: optional hotspot SSID + passphrase so the receiver can auto-join
 */
@Serializable
data class SharePayload(
    val v: Int = 1,
    val u: String = "",
    val s: String? = null,
    val p: String? = null,
) {
    fun encode(): String = Json.encodeToString(SharePayload.serializer(), this)

    companion object {
        fun decode(raw: String): SharePayload? = try {
            Json.decodeFromString(SharePayload.serializer(), raw)
        } catch (_: Exception) {
            null
        }

        /**
         * A scan result is either our JSON payload or a plain share URL
         * (e.g. scanned from the browser web-share page).
         */
        fun fromScan(raw: String): SharePayload? = decode(raw.trim())
    }
}

/** Candidate hosts when the receiver is on the hotspot network. */
fun candidateUrls(base: String): List<String> {
    val trimmed = base.trim().trimEnd('/')
    val urls = arrayListOf(trimmed)
    val host = runCatching { java.net.URI(trimmed).host }.getOrNull()
    if (!host.isNullOrEmpty()) {
        for (gateway in listOf("192.168.43.1", "192.168.42.1")) {
            urls += trimmed.replace(host, gateway)
        }
    }
    return urls.distinct()
}