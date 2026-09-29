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

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.picobeam.app.core.ThemeMode
import com.picobeam.app.core.appSettings
import com.picobeam.app.ui.PicoBeamApp
import com.picobeam.app.ui.theme.PicoBeamTheme
import java.util.Locale

class MainActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(newBase.withAppLocale())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val settings = appSettings(this)

        setContent {
            val themeMode by settings.themeMode.collectAsState()
            val systemDark = isSystemInDarkTheme()
            val dark = when (themeMode) {
                ThemeMode.SYSTEM -> systemDark
                ThemeMode.DARK -> true
                ThemeMode.LIGHT -> false
            }
            PicoBeamTheme(darkTheme = dark) {
                PicoBeamApp()
            }
        }
    }
}

/** Applies the user-selected language to any configuration-created context. */
private fun Context.withAppLocale(): Context {
    val code = appSettings(this).lang.value
    if (code.isBlank() || code == "system") return this
    val locale = Locale.forLanguageTag(code)
    val config = Configuration(resources.configuration)
    config.setLocale(locale)
    return createConfigurationContext(config)
}