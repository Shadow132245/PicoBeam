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

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Creates a device-to-device Wi-Fi hotspot via Android's LocalOnlyHotspot,
 * available to ordinary apps without privileged permissions since API 26.
 *
 * SSID/passphrase are read from the reservation (SoftApConfiguration on
 * API 33+, the legacy WifiConfiguration holder below that), so no private
 * hotspot APIs are needed.
 */
class LocalHotspot(private val context: Context) {

    private val wm: WifiManager =
        context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

    private var reservation: WifiManager.LocalOnlyHotspotReservation? = null

    @Volatile
    var running: Boolean = false
        private set

    suspend fun start(): HotspotInfo? = suspendCancellableCoroutine { cont ->
        val invokeOnStopped = {
            if (cont.isActive) cont.resume(null)
        }

        try {
            wm.startLocalOnlyHotspot(
                object : WifiManager.LocalOnlyHotspotCallback() {
                    override fun onStarted(reservation: WifiManager.LocalOnlyHotspotReservation) {
                        this@LocalHotspot.reservation = reservation
                    }

                    override fun onStopped() = invokeOnStopped()

                    override fun onFailed(reason: Int) = invokeOnStopped()
                },
                Handler(Looper.getMainLooper()),
            )
        } catch (e: Exception) {
            if (cont.isActive) cont.resume(null)
            return@suspendCancellableCoroutine
        }

        cont.invokeOnCancellation { }

        // The reservation arrives asynchronously in onStarted; poll briefly.
        waitForReservation { res ->
            val (ssid, pass) = readConfig(res)
            running = ssid.isNotBlank()
            if (cont.isActive) {
                cont.resume(if (ssid.isBlank()) null else HotspotInfo(ssid, pass, 0))
            }
        }
    }

    /** Reads SSID/passphrase via SoftApConfiguration on 33+, the legacy holder below. */
    private fun readConfig(res: WifiManager.LocalOnlyHotspotReservation): Pair<String, String> {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            runCatching { res.softApConfiguration }.getOrNull()?.let { cfg ->
                val ssid = (cfg.ssid ?: "").trim('"')
                if (ssid.isNotBlank()) return ssid to (cfg.passphrase ?: "")
            }
        }
        runCatching { res.wifiConfiguration }.getOrNull()?.let { cfg ->
            val ssid = (cfg.SSID ?: "").trim('"')
            if (ssid.isNotBlank()) return ssid to (cfg.preSharedKey ?: "")
        }
        return "" to ""
    }

    /**
     * The reservation object arrives in onStarted; on O/P/Q the SSID and
     * passphrase are only available through it. Poll briefly until it lands.
     */
    private fun waitForReservation(consume: (WifiManager.LocalOnlyHotspotReservation) -> Unit) {
        fun poll(retries: Int) {
            val res = reservation
            if (res != null) {
                consume(res)
                return
            }
            if (retries > 0) {
                Handler(Looper.getMainLooper()).postDelayed({ poll(retries - 1) }, 60)
            }
        }
        poll(80)
    }

    fun stop() {
        running = false
        runCatching { reservation?.close() }
        reservation = null
    }
}

/**
 * Joins a Wi-Fi network described by SSID + passphrase using
 * WifiNetworkSpecifier (API 29+). On success the process network is
 * pinned to it so plain HTTP sockets reach the host.
 */
class HotspotJoiner(private val context: Context) {

    suspend fun join(ssid: String, passphrase: String?): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return false
        val cm = context.applicationContext
            .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val builder = android.net.wifi.WifiNetworkSpecifier.Builder()
            .setSsid(ssid)
        if (!passphrase.isNullOrEmpty()) builder.setWpa2Passphrase(passphrase)
        val specifier = builder.build()

        val request = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .addCapability(NetworkCapabilities.NET_CAPABILITY_NOT_RESTRICTED)
            .addCapability(NetworkCapabilities.NET_CAPABILITY_NOT_VPN)
            .setNetworkSpecifier(specifier)
            .build()

        return suspendCancellableCoroutine { cont ->
            val callback = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    runCatching { cm.bindProcessToNetwork(network) }
                    cm.unregisterNetworkCallback(this)
                    if (cont.isActive) cont.resume(true)
                }

                override fun onUnavailable() {
                    cm.unregisterNetworkCallback(this)
                    if (cont.isActive) cont.resume(false)
                }
            }
            try {
                cm.requestNetwork(request, callback)
            } catch (e: Exception) {
                if (cont.isActive) cont.resume(false)
            }
        }
    }
}