package com.haftabook.app.platform

import androidx.compose.runtime.Composable

/**
 * Cross-platform photo picking + local persistence.
 *
 * - Android: gallery + camera.
 * - Desktop: file chooser (gallery).
 * - Web: not supported (returns null / no-op).
 */
interface CustomerPhotoPickerController {
    fun pickFromGallery()
    fun captureFromCamera()
}

@Composable
expect fun rememberCustomerPhotoPicker(
    onImageBytes: (ByteArray) -> Unit,
    onError: (String) -> Unit = {},
): CustomerPhotoPickerController

/** Saves a customer profile photo locally and returns absolute file path. */
expect suspend fun saveCustomerProfilePhoto(
    customerId: Long,
    originalBytes: ByteArray,
): String

/** Reads local photo bytes for display. Returns null if unreadable/missing. */
expect fun readLocalPhotoBytes(path: String): ByteArray?

