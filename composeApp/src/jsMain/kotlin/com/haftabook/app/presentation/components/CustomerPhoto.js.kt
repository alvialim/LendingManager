package com.haftabook.app.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
actual fun CustomerAvatar(
    photoPath: String?,
    displayName: String?,
    modifier: Modifier,
    onClick: (() -> Unit)?,
) {
    // Web: local file paths are not supported; show placeholder.
}

@Composable
actual fun CustomerAvatarBytes(
    photoBytes: ByteArray?,
    displayName: String?,
    modifier: Modifier,
    onClick: (() -> Unit)?,
) {
    // Web: no local storage preview here.
}

@Composable
actual fun CustomerPhotoZoomScreen(photoPath: String, onBack: () -> Unit) {
    // no-op
}

