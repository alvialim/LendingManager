package com.haftabook.app.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
actual fun rememberCustomerPhotoPicker(
    onImageBytes: (ByteArray) -> Unit,
    onError: (String) -> Unit,
): CustomerPhotoPickerController {
    return remember {
        object : CustomerPhotoPickerController {
            override fun pickFromGallery() {
                onError("Photo picking not supported on web")
            }

            override fun captureFromCamera() {
                onError("Camera not supported on web")
            }
        }
    }
}

actual suspend fun saveCustomerProfilePhoto(customerId: Long, originalBytes: ByteArray): String {
    error("Local file storage not supported on web")
}

actual fun readLocalPhotoBytes(path: String): ByteArray? = null

