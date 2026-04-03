package com.haftabook.app.platform

import androidx.compose.runtime.Composable

/**
 * Android: maps system back to [onBack]. Desktop/JS: no-op.
 */
@Composable
expect fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit)
