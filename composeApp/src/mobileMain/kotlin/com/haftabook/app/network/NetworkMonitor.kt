package com.haftabook.app.network

import kotlinx.coroutines.flow.StateFlow

expect class NetworkMonitor() {
    val isOnline: StateFlow<Boolean>
    fun start()
    fun stop()
}
