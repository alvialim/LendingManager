package com.haftabook.app.network

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.net.InetAddress

/**
 * Lightweight connectivity hint for desktop: no platform callback API like Android;
 * treats unknown as online so local DB and sync retries still run.
 *
 * NOTE: ICMP reachability checks often fail on Windows due to firewall rules.
 * We only flip to `true` when we can resolve/reach a host; we do not flip to `false`.
 */
actual class NetworkMonitor {

    private val _isOnline = MutableStateFlow(true)
    actual val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    actual fun start() {
        Thread {
            runCatching {
                InetAddress.getByName("firebase.google.com").isReachable(3_000)
            }.onSuccess { reachable ->
                if (reachable) _isOnline.value = true
            }
        }.start()
    }

    actual fun stop() {
        // no persistent listener
    }
}
