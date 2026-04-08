package com.haftabook.app.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun CustomerAvatar(
    photoPath: String?,
    displayName: String?,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
)

@Composable
expect fun CustomerAvatarBytes(
    photoBytes: ByteArray?,
    displayName: String?,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
)

@Composable
expect fun CustomerPhotoZoomScreen(
    photoPath: String,
    onBack: () -> Unit,
)

