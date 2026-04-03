package com.haftabook.app.platform

import androidx.compose.runtime.Composable

/** Platform status/navigation bar icon contrast vs background (light vs dark theme). */
@Composable
expect fun SystemBarsTheme(darkTheme: Boolean)
