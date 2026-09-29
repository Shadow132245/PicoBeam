package com.picobeam.app.core

import android.graphics.Bitmap
import androidx.camera.core.ImageProxy
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.MultiFormatWriter
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

fun qrBitmap(text: String, sizePx: Int = 1024): ImageBitmap {
    val hints = mapOf(
        EncodeHintType.MARGIN to 1,
        EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.M,
    )
    val matrix = MultiFormatWriter().encode(text, BarcodeFormat.QR_CODE, sizePx, sizePx, hints)
    val pixels = IntArray(sizePx * sizePx)
    for (y in 0 until sizePx) {
        for (x in 0 until sizePx) {
            pixels[y * sizePx + x] = if (matrix.get(x, y)) 0xFF00F0FF.toInt() else 0xFF10223A.toInt()
        }
    }
    return Bitmap.createBitmap(pixels, sizePx, sizePx, Bitmap.Config.ARGB_8888).asImageBitmap()
}

/** Decodes a QR/Codes from a CameraX YUV_420_888 frame. */
fun decodeQr(image: ImageProxy): String? {
    val plane = image.planes[0]
    val buffer = plane.buffer
    val rowStride = plane.rowStride
    val pixelStride = plane.pixelStride
    val width = image.width
    val height = image.height
    if (buffer.remaining() < rowStride * (height - 1) + width) return null
    val yuv = ByteArray(buffer.remaining())
    buffer.get(yuv)
    val pixels = IntArray(width * height)
    for (y in 0 until height) {
        var rowOff = y * rowStride
        var pix = y * width
        val end = rowOff + width * pixelStride
        while (rowOff < end && rowOff < yuv.size) {
            val grey = yuv[rowOff].toInt() and 0xFF
            pixels[pix++] = (0xFF shl 24) or (grey shl 16) or (grey shl 8) or grey
            rowOff += pixelStride
        }
    }
    return decodeQr(pixels, width, height)
}

/** Decodes a QR code from an ARGB pixel array. */
fun decodeQr(pixels: IntArray, width: Int, height: Int): String? {
    return try {
        val source = RGBLuminanceSource(width, height, pixels)
        val bitmap = BinaryBitmap(HybridBinarizer(source))
        val reader = MultiFormatReader()
        reader.setHints(
            mapOf<DecodeHintType, Any>(
                DecodeHintType.POSSIBLE_FORMATS to listOf(BarcodeFormat.QR_CODE),
                DecodeHintType.TRY_HARDER to true,
            ),
        )
        reader.decode(bitmap).text
    } catch (_: Exception) {
        null
    }
}