package com.haftabook.app.platform

import com.haftabook.app.DesktopAppContext

actual fun onSessionUnlocked() {
    DesktopAppContext.startBackgroundSyncIfNeeded()
}

