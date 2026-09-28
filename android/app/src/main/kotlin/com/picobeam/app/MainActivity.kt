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

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.picobeam.app.ui.PicoBeamApp
import com.picobeam.app.ui.theme.PicoBeamTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PicoBeamTheme {
                PicoBeamApp()
            }
        }
    }
}