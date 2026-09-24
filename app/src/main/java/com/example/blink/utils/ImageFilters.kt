package com.example.blink.utils

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.OutputStream
import java.util.ArrayDeque
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sqrt

data class EnhanceParams(
    val brightness: Float = 0f,
    val shadows: Float = 0f,
    val highlights: Float = 0f,
    val contrast: Float = 0f,
    val saturation: Float = 0f,
    val warmth: Float = 0f
)

/**
 * Apply color filter preset and Gemini AI auto-enhance directly to a Bitmap.
 */
fun applyFilterToBitmap(
    bitmap: Bitmap,
    filter: String,
    enhance: EnhanceParams? = null
): Bitmap {
    val width = bitmap.width
    val height = bitmap.height
    val numPixels = width * height
    val pixels = IntArray(numPixels)

    bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

    for (i in 0 until numPixels) {
        val color = pixels[i]
        val a = (color shr 24) and 0xFF

        // If pixel is fully transparent (from background removal), skip processing
        if (a == 0) continue

        var r = ((color shr 16) and 0xFF).toDouble()
        var g = ((color shr 8) and 0xFF).toDouble()
        var b = (color and 0xFF).toDouble()

        // --- STEP 1: Apply Gemini AI Auto Enhance (if enabled) ---
        enhance?.let {
            // 1.1 Brightness / Exposure
            val brightnessShift = it.brightness * 255.0
            r += brightnessShift
            g += brightnessShift
            b += brightnessShift

            // 1.2 Shadows & Highlights
            val lum = 0.299 * r + 0.587 * g + 0.114 * b
            if (lum < 128.0) {
                // Shadow recovery
                val shadowFactor = (1.0 - lum / 128.0) * (it.shadows * 100.0)
                r += shadowFactor
                g += shadowFactor
                b += shadowFactor
            } else {
                // Highlight compression
                val highlightFactor = ((lum - 128.0) / 128.0) * (it.highlights * 100.0)
                r += highlightFactor
                g += highlightFactor
                b += highlightFactor
            }

            // 1.3 Contrast
            if (it.contrast != 0f) {
                val factor = (259.0 * (it.contrast * 255.0 + 255.0)) / (255.0 * (259.0 - it.contrast * 255.0))
                r = factor * (r - 128.0) + 128.0
                g = factor * (g - 128.0) + 128.0
                b = factor * (b - 128.0) + 128.0
            }

            // 1.4 Saturation
            if (it.saturation != 0f) {
                val currentLum = 0.299 * r + 0.587 * g + 0.114 * b
                val satMultiplier = 1.0 + it.saturation
                r = currentLum + (r - currentLum) * satMultiplier
                g = currentLum + (g - currentLum) * satMultiplier
                b = currentLum + (b - currentLum) * satMultiplier
            }

            // 1.5 Warmth (Color Temperature)
            if (it.warmth != 0f) {
                r += it.warmth * 45.0
                b -= it.warmth * 45.0
            }
        }

        // --- STEP 2: Apply One-Touch Color Preset ---
        when (filter.lowercase()) {
            "vintage" -> {
                val gray = 0.299 * r + 0.587 * g + 0.114 * b
                r = r * 0.85 + gray * 0.15 + 20.0
                g = g * 0.85 + gray * 0.10 + 14.0
                b = b * 0.75 + gray * 0.10 + 5.0
                r *= 1.08
                b *= 0.88
            }
            "cinematic" -> {
                val lum = 0.299 * r + 0.587 * g + 0.114 * b
                val contrastFactor = 1.25
                r = (r - 128.0) * contrastFactor + 128.0
                g = (g - 128.0) * contrastFactor + 128.0
                b = (b - 128.0) * contrastFactor + 128.0

                if (lum < 110.0) {
                    r *= 0.85
                    g *= 1.05
                    b *= 1.20
                } else if (lum > 145.0) {
                    r *= 1.18
                    g *= 1.04
                    b *= 0.88
                }
            }
            "bw" -> {
                val gray = 0.299 * r + 0.587 * g + 0.114 * b
                val contrastFactor = 1.35
                val finalGray = min(255.0, max(0.0, (gray - 128.0) * contrastFactor + 128.0))
                r = finalGray
                g = finalGray
                b = finalGray
            }
            "bright" -> {
                r = r * 1.16 + 12.0
                g = g * 1.16 + 12.0
                b = b * 1.16 + 12.0
                val currentLum = 0.299 * r + 0.587 * g + 0.114 * b
                r = currentLum + (r - currentLum) * 1.22
                g = currentLum + (g - currentLum) * 1.22
                b = currentLum + (b - currentLum) * 1.22
            }
            "none" -> {}
        }

        val finalR = min(255, max(0, r.roundToInt()))
        val finalG = min(255, max(0, g.roundToInt()))
        val finalB = min(255, max(0, b.roundToInt()))

        pixels[i] = (a shl 24) or (finalR shl 16) or (finalG shl 8) or finalB
    }

    val resultBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    resultBitmap.setPixels(pixels, 0, width, 0, 0, width, height)
    return resultBitmap
}

/**
 * Intelligent Client-side Background Removal using boundary color sampling,
 * flood-fill segmentation, and feathering.
 */
fun removeBackgroundFromBitmap(
    bitmap: Bitmap,
    tolerance: Float = 38f
): Bitmap {
    val width = bitmap.width
    val height = bitmap.height
    val numPixels = width * height
    val pixels = IntArray(numPixels)

    bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

    val samplePoints = arrayOf(
        intArrayOf(0, 0),
        intArrayOf(width - 1, 0),
        intArrayOf(0, height - 1),
        intArrayOf(width - 1, height - 1),
        intArrayOf(width / 2, 0),
        intArrayOf(width / 4, 0),
        intArrayOf((3 * width) / 4, 0),
        intArrayOf(0, height / 3),
        intArrayOf(width - 1, height / 3),
        intArrayOf(0, (2 * height) / 3),
        intArrayOf(width - 1, (2 * height) / 3)
    )

    data class RGB(val r: Int, val g: Int, val b: Int)
    val bgSeeds = mutableListOf<RGB>()
    for (pt in samplePoints) {
        val color = pixels[pt[1] * width + pt[0]]
        bgSeeds.add(RGB((color shr 16) and 0xFF, (color shr 8) and 0xFF, color and 0xFF))
    }

    val alphaMask = IntArray(numPixels) { 255 }

    fun colorDist(r1: Int, g1: Int, b1: Int, r2: Int, g2: Int, b2: Int): Double {
        val rmean = (r1 + r2) / 2.0
        val dr = (r1 - r2).toDouble()
        val dg = (g1 - g2).toDouble()
        val db = (b1 - b2).toDouble()
        return sqrt(
            (((512.0 + rmean) * dr * dr) / 256.0) +
                    4.0 * dg * dg +
                    (((767.0 - rmean) * db * db) / 256.0)
        )
    }

    val visited = BooleanArray(numPixels)
    val queue = ArrayDeque<Int>()

    for (x in 0 until width) {
        queue.addLast(x)
        queue.addLast((height - 1) * width + x)
    }
    for (y in 1 until height - 1) {
        queue.addLast(y * width)
        queue.addLast(y * width + (width - 1))
    }

    while (queue.isNotEmpty()) {
        val pIdx = queue.removeFirst()
        if (visited[pIdx]) continue
        visited[pIdx] = true

        val px = pIdx % width
        val py = pIdx / width
        val color = pixels[pIdx]
        val r = (color shr 16) and 0xFF
        val g = (color shr 8) and 0xFF
        val b = color and 0xFF

        var minDist = 9999.0
        for (seed in bgSeeds) {
            val d = colorDist(r, g, b, seed.r, seed.g, seed.b)
            if (d < minDist) minDist = d
        }

        val dxFromCenter = abs(px - width / 2.0) / (width / 2.0)
        val dyFromCenter = abs(py - height / 2.0) / (height / 2.0)
        val centerFactor = min(1.0, max(0.4, sqrt(dxFromCenter * dxFromCenter + dyFromCenter * dyFromCenter)))
        val dynamicTolerance = tolerance * (0.6 + 0.6 * centerFactor)

        if (minDist < dynamicTolerance) {
            alphaMask[pIdx] = 0

            if (px > 0 && !visited[pIdx - 1]) queue.addLast(pIdx - 1)
            if (px < width - 1 && !visited[pIdx + 1]) queue.addLast(pIdx + 1)
            if (py > 0 && !visited[pIdx - width]) queue.addLast(pIdx - width)
            if (py < height - 1 && !visited[pIdx + width]) queue.addLast(pIdx + width)
        }
    }

    val featheredMask = IntArray(numPixels)
    for (y in 1 until height - 1) {
        for (x in 1 until width - 1) {
            val idx = y * width + x
            if (alphaMask[idx] == 0) {
                val avg = (alphaMask[idx - 1] + alphaMask[idx + 1] + alphaMask[idx - width] + alphaMask[idx + width]) / 4
                featheredMask[idx] = if (avg > 100) 90 else 0
            } else {
                val n1 = alphaMask[idx - 1]
                val n2 = alphaMask[idx + 1]
                val n3 = alphaMask[idx - width]
                val n4 = alphaMask[idx + width]
                featheredMask[idx] = if (n1 == 0 || n2 == 0 || n3 == 0 || n4 == 0) 180 else 255
            }
        }
    }

    for (i in 0 until numPixels) {
        val alpha = featheredMask[i]
        val color = pixels[i]
        pixels[i] = (alpha shl 24) or (color and 0x00FFFFFF)
    }

    val resultBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    resultBitmap.setPixels(pixels, 0, width, 0, 0, width, height)
    return resultBitmap
}

/**
 * Scale source image and run processing pipeline.
 */
fun renderProcessedImage(
    sourceBitmap: Bitmap,
    filter: String,
    isBgRemoved: Boolean,
    enhanceParams: EnhanceParams? = null,
    maxDimension: Int = 1920
): Bitmap {
    var width = sourceBitmap.width
    var height = sourceBitmap.height

    if (width > maxDimension || height > maxDimension) {
        val scale = maxDimension.toFloat() / max(width, height)
        width = (width * scale).roundToInt()
        height = (height * scale).roundToInt()
    }

    var processed = Bitmap.createScaledBitmap(sourceBitmap, width, height, true)

    if (isBgRemoved) {
        processed = removeBackgroundFromBitmap(processed)
    }

    processed = applyFilterToBitmap(processed, filter, enhanceParams)
    return processed
}

/**
 * Saves image to Android device's MediaStore (Gallery).
 */
fun saveImageToDevice(
    context: Context,
    bitmap: Bitmap,
    filename: String
): Uri? {
    val isPng = filename.endsWith(".png", ignoreCase = true)
    val mimeType = if (isPng) "image/png" else "image/jpeg"
    val compressFormat = if (isPng) Bitmap.CompressFormat.PNG else Bitmap.CompressFormat.JPEG

    val values = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, filename)
        put(MediaStore.Images.Media.MIME_TYPE, mimeType)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/BlinkApp")
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }
    }

    val resolver = context.contentResolver
    val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

    uri?.let {
        val outputStream: OutputStream? = resolver.openOutputStream(it)
        outputStream?.use { stream ->
            bitmap.compress(compressFormat, 95, stream)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.clear()
            values.put(MediaStore.Images.Media.IS_PENDING, 0)
            resolver.update(it, values, null, null)
        }
    }
    return uri
}