package com.haftabook.app.platform

import android.content.Context
import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import java.io.ByteArrayOutputStream
import java.io.File
import kotlin.math.max

@Composable
actual fun rememberCustomerPhotoPicker(
    onImageBytes: (ByteArray) -> Unit,
    onError: (String) -> Unit,
): CustomerPhotoPickerController {
    val context = LocalContext.current

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            if (uri == null) return@rememberLauncherForActivityResult
            runCatching {
                context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            }.getOrNull()?.let(onImageBytes) ?: onError("Could not read image")
        }
    )

    val tempCameraFile = remember { File.createTempFile("customer_photo_", ".jpg", context.cacheDir) }
    val cameraUri = androidx.core.content.FileProvider.getUriForFile(
        context,
        context.packageName + ".fileprovider",
        tempCameraFile
    )
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { ok ->
            if (!ok) return@rememberLauncherForActivityResult
            runCatching { tempCameraFile.readBytes() }.getOrNull()?.let(onImageBytes)
                ?: onError("Could not read camera image")
        }
    )

    return remember {
        object : CustomerPhotoPickerController {
            override fun pickFromGallery() {
                galleryLauncher.launch("image/*")
            }

            override fun captureFromCamera() {
                cameraLauncher.launch(cameraUri)
            }
        }
    }
}

actual fun readLocalPhotoBytes(path: String): ByteArray? {
    val ctx: Context = com.haftabook.app.AndroidAppContext.applicationContext ?: return null
    return runCatching {
        if (path.startsWith("content://")) {
            val uri = Uri.parse(path)
            ctx.contentResolver.openInputStream(uri)?.use { it.readBytes() }
        } else {
            File(path).takeIf { it.exists() }?.readBytes()
        }
    }.getOrNull()
}

actual suspend fun saveCustomerProfilePhoto(customerId: Long, originalBytes: ByteArray): String {
    // Decode
    val decoded = BitmapFactory.decodeByteArray(originalBytes, 0, originalBytes.size)
        ?: error("Invalid image")

    // Scale so that min dimension is at least 256px (don’t upscale too much; cap at original)
    val w = decoded.width
    val h = decoded.height
    val minDim = minOf(w, h)
    val targetMin = 256
    val scale = if (minDim >= targetMin) 1f else (targetMin.toFloat() / minDim.toFloat())
    val targetW = max(1, (w * scale).toInt())
    val targetH = max(1, (h * scale).toInt())
    val scaled = if (scale == 1f) decoded else Bitmap.createScaledBitmap(decoded, targetW, targetH, true)

    // Compress iteratively to <= 200KB (prefer quality reduction first).
    fun compressBytes(quality: Int): ByteArray {
        val out = ByteArrayOutputStream()
        scaled.compress(Bitmap.CompressFormat.JPEG, quality, out)
        return out.toByteArray()
    }

    var q = 92
    var bytes = compressBytes(q)
    while (bytes.size > 200 * 1024 && q > 40) {
        q -= 8
        bytes = compressBytes(q)
    }

    // Save to public gallery (Pictures/Star Group) so the user can see it in Photos/Gallery.
    val ctx: Context = com.haftabook.app.AndroidAppContext.applicationContext
        ?: error("No app context")
    val filename = "customer_${customerId}_${System.currentTimeMillis()}.jpg"
    if (Build.VERSION.SDK_INT >= 29) {
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            put(
                MediaStore.MediaColumns.RELATIVE_PATH,
                Environment.DIRECTORY_PICTURES + File.separator + "Star Group"
            )
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }
        val uri = ctx.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            ?: error("Failed to create MediaStore entry")
        ctx.contentResolver.openOutputStream(uri)?.use { it.write(bytes) }
            ?: error("Failed to write image")
        values.clear()
        values.put(MediaStore.Images.Media.IS_PENDING, 0)
        ctx.contentResolver.update(uri, values, null, null)
        return uri.toString()
    } else {
        @Suppress("DEPRECATION")
        val pictures = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        val dir = File(pictures, "Star Group")
        dir.mkdirs()
        val outFile = File(dir, filename)
        outFile.writeBytes(bytes)
        // Legacy MediaStore insert so Gallery apps pick it up.
        @Suppress("DEPRECATION")
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            put(MediaStore.MediaColumns.DATA, outFile.absolutePath)
        }
        val uri = ctx.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        return (uri?.toString() ?: outFile.absolutePath)
    }
}

