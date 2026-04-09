package com.haftabook.app.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.haftabook.app.platform.readLocalPhotoBytes
import org.jetbrains.skia.Image as SkiaImage
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.ui.unit.sp
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private fun decodeSkia(bytes: ByteArray): ImageBitmap? =
    runCatching { SkiaImage.makeFromEncoded(bytes).toComposeImageBitmap() }.getOrNull()

@Composable
actual fun CustomerAvatar(
    photoPath: String?,
    displayName: String?,
    modifier: Modifier,
    onClick: (() -> Unit)?,
) {
    val bitmap: ImageBitmap? by produceState<ImageBitmap?>(initialValue = null, key1 = photoPath) {
        value = if (photoPath.isNullOrBlank()) null else withContext(Dispatchers.IO) {
            val bytes = readLocalPhotoBytes(photoPath)
            bytes?.let(::decodeSkia)
        }
    }
    val base = modifier
        .size(60.dp)
        .clip(CircleShape)
        .border(BorderStroke(1.dp, Color.Gray), CircleShape)
    val clickable = if (onClick != null) base.clickable(onClick = onClick) else base
    bitmap?.let { img ->
        Image(
            bitmap = img,
            contentDescription = "Customer photo",
            modifier = clickable,
            contentScale = ContentScale.Crop,
        )
    } ?: run {
        val bg = placeholderColor(displayName)
        val initial = placeholderInitial(displayName)
        Box(
            modifier = clickable.background(bg),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initial,
                color = Color.White,
                fontSize = 18.sp,

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
    val bitmap: ImageBitmap? by produceState<ImageBitmap?>(initialValue = null, key1 = photoBytes) {
        value = if (photoBytes == null || photoBytes.isEmpty()) {
            null
        } else {
            withContext(Dispatchers.IO) {
                decodeSkia(photoBytes)
            }
        }
    }
    val base = modifier
        .size(60.dp)
        .clip(CircleShape)
        .border(BorderStroke(1.dp, Color.Gray), CircleShape)
    val clickable = if (onClick != null) base.clickable(onClick = onClick) else base
    bitmap?.let { img ->
        Image(
            bitmap = img,
            contentDescription = "Customer photo",
            modifier = clickable,
            contentScale = ContentScale.Crop,
        )
    } ?: run {
        val bg = placeholderColor(displayName)
        val initial = placeholderInitial(displayName)
        Box(
            modifier = clickable.background(bg),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initial,
                color = Color.White,
                fontSize = 18.sp,

                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
actual fun CustomerPhotoZoomScreen(photoPath: String, onBack: () -> Unit) {
    val lastModified = remember(photoPath) { runCatching { File(photoPath).lastModified() }.getOrNull() }
    val bitmap: ImageBitmap? by produceState<ImageBitmap?>(initialValue = null, key1 = photoPath, key2 = lastModified) {
        value = withContext(Dispatchers.IO) {
            val bytes = readLocalPhotoBytes(photoPath)
            bytes?.let(::decodeSkia)
        }
    }

    var scale by remember { mutableStateOf(1f) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    val state = rememberTransformableState { zoomChange, panChange, _ ->
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
    ) { _ ->
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
            bitmap?.let { img ->
                Image(bitmap = img, contentDescription = "Customer photo")
            }
        }
    }
}
