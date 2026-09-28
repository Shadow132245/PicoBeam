package com.picobeam.app.core

import java.net.Inet4Address
import java.net.NetworkInterface

/** First non-loopback IPv4 address usable on the LAN. */
fun lanIpv4(): String? {
    NetworkInterface.getNetworkInterfaces()?.toList()?.forEach { nif ->
        if (!nif.isUp || nif.isLoopback) return@forEach
        nif.inetAddresses?.toList()?.forEach { addr ->
            if (addr !is Inet4Address || addr.isLoopbackAddress) return@forEach
            return addr.hostAddress
        }
    }
    return null
}