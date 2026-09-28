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