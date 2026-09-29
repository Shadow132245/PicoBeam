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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

enum class ThemeMode { SYSTEM, DARK, LIGHT }

class AppSettings(context: Context) {

    private val prefs = context.getSharedPreferences("picobeam", Context.MODE_PRIVATE)

    val themeMode = MutableStateFlow(
        ThemeMode.valueOf(prefs.getString(KEY_THEME, ThemeMode.SYSTEM.name) ?: ThemeMode.SYSTEM.name),
    )
    val lang = MutableStateFlow(prefs.getString(KEY_LANG, "en") ?: "en")

    fun setThemeMode(mode: ThemeMode) {
        themeMode.value = mode
        prefs.edit().putString(KEY_THEME, mode.name).apply()
    }

    fun setLang(code: String) {
        lang.value = code
        prefs.edit().putString(KEY_LANG, code).apply()
    }

    companion object {
        private const val KEY_THEME = "theme"
        private const val KEY_LANG = "lang"
    }
}

/** Minimal shared single instance (single-activity app). */
private var instance: AppSettings? = null

fun appSettings(context: Context): AppSettings {
    return instance ?: AppSettings(context.applicationContext).also { instance = it }
}

class HotspotInfo(val ssid: String, val passphrase: String?, val frequency: Int) {
    override fun toString(): String = "HotspotInfo(ssid=$ssid, freq=$frequency)"
}