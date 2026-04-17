package com.haftabook.app.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Simple responsive wrapper:
 * - On phones: content is full width.
 * - On large windows (desktop/tablet): content is centered with a max width cap.
 */
@Composable
fun ResponsiveCentered(
    modifier: Modifier = Modifier,
    compactMaxWidth: Dp = 560.dp,
    expandedMaxWidth: Dp = 520.dp,
    contentAlignment: Alignment = Alignment.Center,
    content: @Composable (Modifier) -> Unit,
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isCompact = maxWidth <= compactMaxWidth
        val inner = Modifier
            .widthIn(max = if (isCompact) Dp.Unspecified else expandedMaxWidth)
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = contentAlignment) {
            content(inner)
        }
    }
}

