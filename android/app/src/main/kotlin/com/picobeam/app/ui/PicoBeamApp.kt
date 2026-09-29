/*
 * PicoBeam — peer-to-peer file transfer for Android
 * Copyright (C) 2026 Hassan (a.k.a. EuroMoscow)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.picobeam.app.ui

import android.app.Activity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.picobeam.app.HubViewModel
import com.picobeam.app.R
import com.picobeam.app.core.ThemeMode

private val LANGS = listOf(
    "English" to "en",
    "العربية" to "ar",
    "Français" to "fr",
    "Español" to "es",
)

private val THEME_ICONS = mapOf(
    ThemeMode.SYSTEM to Icons.Filled.BrightnessAuto,
    ThemeMode.DARK to Icons.Filled.DarkMode,
    ThemeMode.LIGHT to Icons.Filled.WbSunny,
)

private fun ThemeMode.next() = when (this) {
    ThemeMode.SYSTEM -> ThemeMode.DARK
    ThemeMode.DARK -> ThemeMode.LIGHT
    ThemeMode.LIGHT -> ThemeMode.SYSTEM
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PicoBeamApp(vm: HubViewModel = viewModel()) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val themeMode by vm.themeMode.collectAsState()
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Row {
                        Icon(
                            Icons.Filled.RocketLaunch,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                        )
                        Text(
                            "PicoBeam",
                            modifier = Modifier.padding(start = 8.dp),
                            style = MaterialTheme.typography.titleLarge,
                        )
                    }
                },
                actions = {
                    LanguageMenu(vm)
                    IconButton(onClick = { vm.setThemeMode(themeMode.next()) }) {
                        val icon: ImageVector = THEME_ICONS[themeMode] ?: Icons.Filled.BrightnessAuto
                        Icon(
                            icon,
                            contentDescription = when (themeMode) {
                                ThemeMode.SYSTEM -> "Theme: system"
                                ThemeMode.DARK -> context.getString(R.string.theme_toggle_dark)
                                ThemeMode.LIGHT -> context.getString(R.string.theme_toggle_light)
                            },
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                ),
            )
        },
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                NavigationBarItem(
                    selected = currentRoute == "share",
                    onClick = {
                        navController.navigate("share") {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = { Icon(Icons.Filled.Send, contentDescription = null) },
                    label = { Text(context.getString(R.string.share_tab)) },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    ),
                )
                NavigationBarItem(
                    selected = currentRoute == "receive",
                    onClick = {
                        navController.navigate("receive") {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = { Icon(Icons.Filled.FileDownload, contentDescription = null) },
                    label = { Text(context.getString(R.string.receive_tab)) },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    ),
                )
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "share",
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            composable("share") { ShareScreen(vm) }
            composable("receive") { ReceiveScreen(vm) }
        }
    }
}

@Composable
private fun LanguageMenu(vm: HubViewModel) {
    val context = LocalContext.current
    var open by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { open = true }) {
            Icon(
                Icons.Filled.Translate,
                contentDescription = "Language",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            LANGS.forEach { (label, code) ->
                DropdownMenuItem(
                    text = { Text(label) },
                    onClick = {
                        open = false
                        vm.setLang(code)
                        runCatching { (context as Activity).recreate() }
                    },
                )
            }
        }
    }
}