package com.haftabook.app.platform

actual fun onSessionUnlocked() {
    // no-op on Android (sync already managed by platform/services)
}

