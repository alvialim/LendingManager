package com.haftabook.app.data.sync

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Lightweight runtime diagnostics for packaged builds.
 * Used to surface Firebase sync failures inside the UI (Desktop MSI especially).
 */
object SyncDiagnostics {
    private val _lastLog = MutableStateFlow<String?>(null)
    val lastLog: StateFlow<String?> = _lastLog.asStateFlow()

    private val _lastError = MutableStateFlow<String?>(null)
    val lastError: StateFlow<String?> = _lastError.asStateFlow()

    fun noteLog(message: String) {
        _lastLog.value = message.take(400)
    }

    fun noteError(message: String) {
        _lastError.value = message.take(800)
        _lastLog.value = _lastError.value
    }
}

