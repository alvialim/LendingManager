package com.haftabook.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.haftabook.app.data.remote.CustomerRemote
import com.haftabook.app.ui.HaftabookTheme
import com.haftabook.app.data.sync.FIRESTORE_COLLECTION_CUSTOMERS
import com.haftabook.app.data.sync.HAFTABOOK_REALTIME_DATABASE_URL
import com.haftabook.app.web.ensureWebFirebaseInitialized
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.database.database
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.retry

@Composable
actual fun AppRoot() {
    HaftabookTheme {
        var customers by remember { mutableStateOf<Map<String, CustomerRemote>>(emptyMap()) }
        var error by remember { mutableStateOf<String?>(null) }
        var loading by remember { mutableStateOf(true) }

        LaunchedEffect(Unit) {
            val init = runCatching { ensureWebFirebaseInitialized() }
            if (init.isFailure) {
                loading = false
                error = init.exceptionOrNull()?.message ?: init.toString()
                return@LaunchedEffect
            }
            try {
                val customersRef = Firebase.database(HAFTABOOK_REALTIME_DATABASE_URL)
                    .reference(FIRESTORE_COLLECTION_CUSTOMERS)
                customersRef.valueEvents
                    .retry { cause ->
                        if (cause is CancellationException) return@retry false
                        delay(10_000L)
                        true
                    }
                    .catch { t ->
                        loading = false
                        error = t.message ?: t.toString()
                    }
                    .collectLatest { snap ->
                        loading = false
                        error = null
                        customers = snap.value<Map<String, CustomerRemote>?>() ?: emptyMap()
                    }
            } catch (t: Throwable) {
                if (t !is CancellationException) {
                    loading = false
                    error = t.message ?: t.toString()
                }
            }
        }

        Surface(modifier = Modifier.fillMaxSize(), color = androidx.compose.material3.MaterialTheme.colorScheme.background) {
            when {
                loading && customers.isEmpty() && error == null -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                error != null -> {
                    Box(
                        Modifier.fillMaxSize().padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Firebase: $error\n\nCheck Realtime Database rules and Web app config (see WebFirebaseInit.kt).",
                            color = Color(0xFFB71C1C),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
                else -> {
                    Column(Modifier.fillMaxSize().padding(16.dp)) {
                        Text(
                            text = "Customers (Realtime Database)",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color(0xFF1A237E),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Text(
                            text = "Android: Room SQLite + this same RTDB path. Web: no Room (browser); data loads live from Firebase.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF3949AB),
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(customers.values.sortedBy { it.name.lowercase() }) { c ->
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    color = Color.White,
                                    tonalElevation = 1.dp
                                ) {
                                    Column(Modifier.padding(12.dp)) {
                                        Text(c.name, style = MaterialTheme.typography.titleMedium)
                                        Text(
                                            "${c.mobile} · ${c.loanType}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Color(0xFF616161)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
