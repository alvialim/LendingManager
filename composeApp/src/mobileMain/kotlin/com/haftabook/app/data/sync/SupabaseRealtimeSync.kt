package com.haftabook.app.data.sync

import com.haftabook.app.data.remote.SupabaseConfig
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.http.encodeURLParameter
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Subscribes to Supabase Realtime [postgres_changes](https://supabase.com/docs/guides/realtime/postgres-changes).
 * Tables must be added to the `supabase_realtime` publication (see [supabase/schema.sql]).
 * If the socket fails, [SupabaseSyncEngine] still syncs via REST polling.
 */
class SupabaseRealtimeSync(
    private val client: HttpClient,
    private val config: SupabaseConfig,
    private val onRemoteChange: suspend () -> Unit
) {
    private var job: Job? = null
    private val refMutex = Mutex()
    private var msgRef = 1

    private suspend fun nextRef(): String = refMutex.withLock { (msgRef++).toString() }

    fun start(scope: CoroutineScope) {
        job?.cancel()
        job = scope.launch(Dispatchers.Default) {
            while (isActive) {
                runCatching { runRealtimeSession() }
                delay(RECONNECT_DELAY_MS)
            }
        }
    }

    fun stop() {
        job?.cancel()
        job = null
    }

    private suspend fun runRealtimeSession() {
        refMutex.withLock { msgRef = 1 }
        val url = buildRealtimeWsUrl()
        client.webSocket(url) {
            supervisorScope {
                val heartbeat = launch {
                    while (isActive) {
                        delay(HEARTBEAT_MS)
                        send(
                            Frame.Text(
                                """{"topic":"phoenix","event":"heartbeat","payload":{},"ref":"${nextRef()}"}"""
                            )
                        )
                    }
                }
                try {
                    for (table in listOf("customers", "loans", "emis")) {
                        val join =
                            """{"topic":"realtime:public:$table","event":"phx_join","payload":{"config":{"postgres_changes":[{"event":"*","schema":"public","table":"$table"}]},"access_token":"${escapeJson(config.anonKey)}"},"ref":"${nextRef()}"}"""
                        send(Frame.Text(join))
                    }
                    while (isActive) {
                        when (val frame = incoming.receive()) {
                            is Frame.Text -> {
                                val text = frame.readText()
                                if (text.contains("postgres_changes") ||
                                    text.contains("\"event\":\"INSERT\"") ||
                                    text.contains("\"event\":\"UPDATE\"") ||
                                    text.contains("\"event\":\"DELETE\"")
                                ) {
                                    runCatching { onRemoteChange() }
                                }
                            }
                            else -> {}
                        }
                    }
                } finally {
                    heartbeat.cancel()
                }
            }
        }
    }

    private fun buildRealtimeWsUrl(): String {
        val trimmed = config.url.trimEnd('/')
        val host = trimmed.substringAfter("://").substringBefore("/")
        val scheme = if (trimmed.startsWith("https", ignoreCase = true)) "wss" else "ws"
        val key = config.anonKey.encodeURLParameter()
        return "$scheme://$host/realtime/v1/websocket?apikey=$key&vsn=1.0.0"
    }

    private fun escapeJson(s: String): String =
        s.replace("\\", "\\\\").replace("\"", "\\\"")

    companion object {
        private const val HEARTBEAT_MS = 25_000L
        private const val RECONNECT_DELAY_MS = 5_000L
    }
}
