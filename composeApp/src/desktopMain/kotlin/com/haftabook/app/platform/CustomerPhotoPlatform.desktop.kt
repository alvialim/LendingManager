package com.haftabook.app.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import java.awt.FileDialog
import java.awt.Frame
import java.awt.Toolkit
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import javax.imageio.IIOImage
import javax.imageio.ImageIO
import javax.imageio.ImageWriteParam
import javax.imageio.ImageWriter
import javax.imageio.stream.ImageOutputStream
import kotlin.math.max

@Composable
actual fun rememberCustomerPhotoPicker(
    onImageBytes: (ByteArray) -> Unit,
    onError: (String) -> Unit,
): CustomerPhotoPickerController {
    return remember {
        object : CustomerPhotoPickerController {
            override fun pickFromGallery() {
                runCatching {
                    val fd = FileDialog(Frame(), "Select customer photo", FileDialog.LOAD)
                    fd.isVisible = true
                    val file = fd.file ?: return
                    val dir = fd.directory ?: ""
                    val f = File(dir, file)
                    if (!f.exists()) return
                    onImageBytes(f.readBytes())
                }.onFailure { onError(it.message ?: "Failed to pick image") }
            }

            override fun captureFromCamera() {
                // Not supported on desktop; fall back to file picker.
                pickFromGallery()
            }
        }
    }
}

actual fun readLocalPhotoBytes(path: String): ByteArray? =
    runCatching { File(path).takeIf { it.exists() }?.readBytes() }.getOrNull()

actual suspend fun saveCustomerProfilePhoto(customerId: Long, originalBytes: ByteArray): String {
    val originalImage = ImageIO.read(ByteArrayInputStream(originalBytes)) ?: error("Invalid image")

    val w = originalImage.width
    val h = originalImage.height
    val minDim = minOf(w, h)
    val targetMin = 256
    val scale = if (minDim >= targetMin) 1.0 else (targetMin.toDouble() / minDim.toDouble())
    val targetW = max(1, (w * scale).toInt())
    val targetH = max(1, (h * scale).toInt())
    val scaled = if (scale == 1.0) originalImage else {
        val out = BufferedImage(targetW, targetH, BufferedImage.TYPE_INT_RGB)
        val g = out.createGraphics()
        g.drawImage(originalImage, 0, 0, targetW, targetH, null)
        g.dispose()
        out
    }

    fun compressJpegBytes(img: BufferedImage, quality: Float): ByteArray {
        val baos = ByteArrayOutputStream()
        val writers: Iterator<ImageWriter> = ImageIO.getImageWritersByFormatName("jpg")
        val writer = writers.next()
        val ios: ImageOutputStream = ImageIO.createImageOutputStream(baos)
        writer.output = ios
        val params = writer.defaultWriteParam
        if (params.canWriteCompressed()) {
            params.compressionMode = ImageWriteParam.MODE_EXPLICIT
            params.compressionQuality = quality.coerceIn(0.05f, 1.0f)
        }
        writer.write(null, IIOImage(img, null, null), params)
        ios.close()
        writer.dispose()
        return baos.toByteArray()
    }

    var quality = 0.92f
    var bytes = compressJpegBytes(scaled, quality)
    while (bytes.size > 200 * 1024 && quality > 0.40f) {
        quality -= 0.08f
        bytes = compressJpegBytes(scaled, quality)
    }

    val dir = File(System.getProperty("user.home"), ".star-group/customer_photos")
    dir.mkdirs()
    val outFile = File(dir, "customer_${customerId}_${System.currentTimeMillis()}.jpg")
    outFile.writeBytes(bytes)
    return outFile.absolutePath
}

