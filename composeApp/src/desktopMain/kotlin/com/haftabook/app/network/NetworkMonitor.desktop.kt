package com.haftabook.app.network

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Desktop (Windows/macOS/Linux): do **not** use ICMP/DNS reachability.
 * On Windows, [InetAddress.isReachable] often blocks for seconds or fails behind firewalls,
 * and extra threads compete with Compose/Skia.
 *
 * Treat as online so Room + Firebase sync keep working; sync failures are handled by retries.
 */
actual class NetworkMonitor {

    private val _isOnline = MutableStateFlow(true)
    actual val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    actual fun start() {
        // Intentionally empty — no background threads, no blocking network probes.
    }

    actual fun stop() {
        // no persistent listener
    }
}
