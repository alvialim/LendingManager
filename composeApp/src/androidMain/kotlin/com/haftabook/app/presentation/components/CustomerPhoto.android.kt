package com.haftabook.app.presentation.components

import android.graphics.BitmapFactory
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Text
import com.haftabook.app.platform.readLocalPhotoBytes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
actual fun CustomerAvatar(
    photoPath: String?,
    displayName: String?,
    modifier: Modifier,
    onClick: (() -> Unit)?,
) {
    val bitmap by produceState(initialValue = null, key1 = photoPath) {
        value = if (photoPath.isNullOrBlank()) null else withContext(Dispatchers.IO) {
            val bytes = readLocalPhotoBytes(photoPath)
            bytes?.let { BitmapFactory.decodeByteArray(it, 0, it.size) }?.asImageBitmap()
        }
    }
    val base = modifier
        .size(60.dp)
        .clip(CircleShape)
        .border(BorderStroke(1.dp, Color.Gray), CircleShape)
    val clickable = if (onClick != null) base.clickable(onClick = onClick) else base
    if (bitmap != null) {
        Image(bitmap = bitmap, contentDescription = "Customer photo", modifier = clickable)
    } else {
        val bg = placeholderColor(displayName)
        val initial = placeholderInitial(displayName)
        Box(
            modifier = clickable.background(bg),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initial,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
actual fun CustomerAvatarBytes(
    photoBytes: ByteArray?,
    displayName: String?,
    modifier: Modifier,
    onClick: (() -> Unit)?,
) {
    val bitmap = remember(photoBytes) {
        photoBytes?.let { BitmapFactory.decodeByteArray(it, 0, it.size) }?.asImageBitmap()
    }
    val base = modifier
        .size(60.dp)
        .clip(CircleShape)
        .border(BorderStroke(1.dp, Color.Gray), CircleShape)
    val clickable = if (onClick != null) base.clickable(onClick = onClick) else base
    if (bitmap != null) {
        Image(bitmap = bitmap, contentDescription = "Customer photo", modifier = clickable)
    } else {
        val bg = placeholderColor(displayName)
        val initial = placeholderInitial(displayName)
        Box(
            modifier = clickable.background(bg),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initial,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
actual fun CustomerPhotoZoomScreen(photoPath: String, onBack: () -> Unit) {
    val bytes = remember(photoPath) { readLocalPhotoBytes(photoPath) }
    val bitmap = remember(bytes) {
        bytes?.let { BitmapFactory.decodeByteArray(it, 0, it.size) }?.asImageBitmap()
    }

    var scale by remember { mutableStateOf(1f) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    val state = androidx.compose.foundation.gestures.rememberTransformableState { zoomChange, panChange, _ ->
        scale = (scale * zoomChange).coerceIn(0.8f, 6f)
        offsetX += panChange.x
        offsetY += panChange.y
    }

    @OptIn(ExperimentalMaterial3Api::class)
    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .transformable(state)
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offsetX,
                    translationY = offsetY
                ),
            contentAlignment = Alignment.Center
        ) {
            if (bitmap != null) {
                Image(bitmap = bitmap, contentDescription = "Customer photo")
            }
        }
    }
}

