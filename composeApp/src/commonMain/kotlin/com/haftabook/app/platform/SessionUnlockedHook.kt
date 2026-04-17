package com.haftabook.app.platform

/**
 * Platform hook invoked once after the app session is unlocked (PIN entered).
 *
 * Desktop uses this to start background sync only after the lock screen, so low-end
 * machines stay responsive during initial UI.
 */
expect fun onSessionUnlocked()

