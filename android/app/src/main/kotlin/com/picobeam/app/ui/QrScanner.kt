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

import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.util.Size
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.picobeam.app.R
import com.picobeam.app.core.decodeQr
import java.util.concurrent.atomic.AtomicBoolean

/** Finds the nearest [LifecycleOwner] walking up the context wrapper chain. */
private tailrec fun findLifecycleOwner(context: Context): LifecycleOwner? {
    if (context is LifecycleOwner) return context
    return if (context is ContextWrapper) findLifecycleOwner(context.baseContext) else null
}

@androidx.annotation.OptIn(ExperimentalGetImage::class)
@Composable
fun QrScanner(onScanned: (String) -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val lifecycleOwner = remember { findLifecycleOwner(context) }
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED,
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted -> hasPermission = granted }

    if (!hasPermission) {
        Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Button(onClick = { permissionLauncher.launch(android.Manifest.permission.CAMERA) }) {
                Text(context.getString(R.string.allow_camera))
            }
        }
        return
    }

    if (lifecycleOwner == null) {
        Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                context.getString(R.string.camera_unavailable),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        return
    }

    val scanned = remember { AtomicBoolean(false) }
    val disposed = remember { AtomicBoolean(false) }

    DisposableEffect(Unit) {
        onDispose {
            disposed.set(true)
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { viewContext ->
            PreviewView(viewContext).also { pv ->
                pv.scaleType = PreviewView.ScaleType.FILL_CENTER
                pv.layoutParams = FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                )
                val providerFuture = ProcessCameraProvider.getInstance(viewContext)
                providerFuture.addListener(
                    {
                        val provider = runCatching { providerFuture.get() }.getOrNull()
                            ?: return@addListener
                        if (disposed.get()) {
                            provider.unbindAll()
                            return@addListener
                        }
                        val preview = Preview.Builder().build().also {
                            it.setSurfaceProvider(pv.surfaceProvider)
                        }
                        val analysis = ImageAnalysis.Builder()
                            .setTargetResolution(Size(1024, 1024))
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build()
                            .also { ia ->
                                ia.setAnalyzer(ContextCompat.getMainExecutor(viewContext)) { image ->
                                    try {
                                        if (!scanned.get()) {
                                            val text = decodeQr(image)
                                            if (text != null && scanned.compareAndSet(false, true)) {
                                                onScanned(text)
                                            }
                                        }
                                    } finally {
                                        image.close()
                                    }
                                }
                            }
                        runCatching {
                            provider.bindToLifecycle(
                                lifecycleOwner,
                                CameraSelector.DEFAULT_BACK_CAMERA,
                                preview,
                                analysis,
                            )
                        }
                    },
                    ContextCompat.getMainExecutor(viewContext),
                )
            }
        },
    )
}