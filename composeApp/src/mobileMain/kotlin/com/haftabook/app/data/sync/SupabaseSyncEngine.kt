package com.haftabook.app.data.sync

import com.haftabook.app.data.AppDatabase
import com.haftabook.app.data.local.entity.CustomerEntity
import com.haftabook.app.data.local.entity.EmiEntity
import com.haftabook.app.data.local.entity.LoanEntity
import com.haftabook.app.data.local.entity.SyncStateEntity
import com.haftabook.app.data.remote.CustomerRemote
import com.haftabook.app.data.remote.EmiRemote
import com.haftabook.app.data.remote.LoanRemote
import com.haftabook.app.data.remote.SupabaseConfig
import com.haftabook.app.data.remote.SupabaseRestApi
import com.haftabook.app.network.NetworkMonitor
import io.ktor.client.HttpClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json

/**
 * Offline-first sync:
 * - Local writes go to Room + outbox immediately (works offline).
 * - When online, [drainOutbox] pushes to PostgREST; [pullRemote] merges server rows (last-write-wins on [updated_at]).
 * - [SupabaseRealtimeSync] triggers [pullRemote] when other devices change data (requires Realtime publication in SQL).
 */
class SupabaseSyncEngine(
    private val db: AppDatabase,
    private val api: SupabaseRestApi?,
    private val supabaseConfig: SupabaseConfig?,
    private val networkMonitor: NetworkMonitor,
    private val httpClient: HttpClient,
    private val json: Json = Json { ignoreUnknownKeys = true; encodeDefaults = true }
) {
    private val syncMutex = Mutex()
    private var realtime: SupabaseRealtimeSync? = null

    fun start(scope: CoroutineScope) {
        if (api == null || supabaseConfig == null) return
        networkMonitor.start()

        realtime = SupabaseRealtimeSync(httpClient, supabaseConfig) {
            syncMutex.withLock { pullRemote() }
        }.also { it.start(scope) }

        // When connectivity is available: sync immediately and then on a short interval while online.
        scope.launch(Dispatchers.Default) {
            networkMonitor.isOnline.collectLatest { online ->
                if (!online) return@collectLatest
                while (isActive && networkMonitor.isOnline.value) {
                    syncMutex.withLock {
                        runCatching { drainOutbox() }
                        runCatching { pullRemote() }
                    }
                    delay(POLL_INTERVAL_MS)
                }
            }
        }
    }

    fun stop() {
        realtime?.stop()
        realtime = null
        networkMonitor.stop()
    }

    /**
     * Manual sync (e.g. pull-to-refresh). No-op if Supabase is not configured.
     */
    suspend fun syncNow() {
        if (api == null) return
        syncMutex.withLock {
            runCatching { drainOutbox() }
            runCatching { pullRemote() }
        }
    }

    private suspend fun drainOutbox() {
        val api = this.api ?: return
        val rows = db.syncOutboxDao().peek(25)
        for (row in rows) {
            val result = runCatching {
                when (row.entityType) {
                    SyncEntityType.CUSTOMER -> when (row.operation) {
                        SyncOperation.UPSERT -> {
                            val payload = json.decodeFromString(CustomerRemote.serializer(), row.payloadJson)
                            api.upsertCustomer(payload).getOrThrow()
                        }
                        SyncOperation.DELETE -> {
                            val p = json.decodeFromString(CustomerDeletePayload.serializer(), row.payloadJson)
                            api.deleteCustomer(p.remoteId).getOrThrow()
                        }
                        else -> error("bad op")
                    }
                    SyncEntityType.LOAN -> when (row.operation) {
                        SyncOperation.UPSERT -> {
                            val payload = json.decodeFromString(LoanRemote.serializer(), row.payloadJson)
                            api.upsertLoan(payload).getOrThrow()
                        }
                        SyncOperation.DELETE -> {
                            val p = json.decodeFromString(LoanDeletePayload.serializer(), row.payloadJson)
                            api.deleteLoan(p.remoteId).getOrThrow()
                        }
                        else -> error("bad op")
                    }
                    SyncEntityType.EMI -> when (row.operation) {
                        SyncOperation.UPSERT -> {
                            val payload = json.decodeFromString(EmiRemote.serializer(), row.payloadJson)
                            api.upsertEmi(payload).getOrThrow()
                        }
                        SyncOperation.DELETE -> {
                            val p = json.decodeFromString(EmiDeletePayload.serializer(), row.payloadJson)
                            api.deleteEmi(p.remoteId).getOrThrow()
                        }
                        else -> error("bad op")
                    }
                    else -> error("unknown entity")
                }
            }
            if (result.isSuccess) {
                db.syncOutboxDao().deleteById(row.id)
            } else {
                val err = result.exceptionOrNull()?.message ?: "error"
                db.syncOutboxDao().markFailure(row.id, err)
            }
        }
    }

    private suspend fun pullRemote() {
        val api = this.api ?: return
        val state = db.syncStateDao()
        val customerCursor = state.getValue(SyncStateKeys.LAST_PULL_CUSTOMERS)?.toLongOrNull() ?: 0L
        val loanCursor = state.getValue(SyncStateKeys.LAST_PULL_LOANS)?.toLongOrNull() ?: 0L
        val emiCursor = state.getValue(SyncStateKeys.LAST_PULL_EMIS)?.toLongOrNull() ?: 0L

        val customers = api.fetchCustomersSince(customerCursor)
        var maxC = customerCursor
        for (r in customers) {
            mergeCustomer(r)
            if (r.updatedAt > maxC) maxC = r.updatedAt
        }
        if (customers.isNotEmpty()) {
            state.upsert(SyncStateEntity(SyncStateKeys.LAST_PULL_CUSTOMERS, maxC.toString()))
        }

        val loans = api.fetchLoansSince(loanCursor)
        var maxL = loanCursor
        for (r in loans) {
            mergeLoan(r)
            if (r.updatedAt > maxL) maxL = r.updatedAt
        }
        if (loans.isNotEmpty()) {
            state.upsert(SyncStateEntity(SyncStateKeys.LAST_PULL_LOANS, maxL.toString()))
        }

        val emis = api.fetchEmisSince(emiCursor)
        var maxE = emiCursor
        for (r in emis) {
            mergeEmi(r)
            if (r.updatedAt > maxE) maxE = r.updatedAt
        }
        if (emis.isNotEmpty()) {
            state.upsert(SyncStateEntity(SyncStateKeys.LAST_PULL_EMIS, maxE.toString()))
        }
    }

    private suspend fun mergeCustomer(r: CustomerRemote) {
        val dao = db.customerDao()
        val existing = dao.getCustomerByRemoteId(r.id)
        if (existing == null) {
            dao.insertCustomer(
                CustomerEntity(
                    id = 0,
                    remoteId = r.id,
                    updatedAt = r.updatedAt,
                    name = r.name,
                    mobile = r.mobile,
                    loanType = r.loanType,
                    createdDate = r.createdDate
                )
            )
        } else if (r.updatedAt > existing.updatedAt) {
            dao.insertCustomer(
                existing.copy(
                    name = r.name,
                    mobile = r.mobile,
                    loanType = r.loanType,
                    createdDate = r.createdDate,
                    updatedAt = r.updatedAt
                )
            )
        }
    }

    private suspend fun mergeLoan(r: LoanRemote) {
        val customer = db.customerDao().getCustomerByRemoteId(r.customerId) ?: return
        val dao = db.loanDao()
        val existing = dao.getLoanByRemoteId(r.id)
        val row = LoanEntity(
            id = existing?.id ?: 0,
            remoteId = r.id,
            updatedAt = r.updatedAt,
            customerId = customer.id,
            loanNumber = r.loanNumber,
            loanAmount = r.loanAmount,
            emiAmount = r.emiAmount,
            loanStartDate = r.loanStartDate,
            emiStartDate = r.emiStartDate,
            totalEmis = r.totalEmis,
            lastEmiDate = r.lastEmiDate,
            remainingAmount = r.remainingAmount
        )
        if (existing == null) {
            dao.insertLoan(row)
        } else if (r.updatedAt > existing.updatedAt) {
            dao.insertLoan(row.copy(id = existing.id))
        }
    }

    private suspend fun mergeEmi(r: EmiRemote) {
        val loan = db.loanDao().getLoanByRemoteId(r.loanId) ?: return
        val dao = db.emiDao()
        val existing = dao.getEmiByRemoteId(r.id)
        val row = EmiEntity(
            id = existing?.id ?: 0,
            remoteId = r.id,
            updatedAt = r.updatedAt,
            loanId = loan.id,
            emiNumber = r.emiNumber,
            emiAmount = r.emiAmount,
            emiDate = r.emiDate,
            createdAt = r.createdAt
        )
        if (existing == null) {
            dao.insertEmi(row)
        } else if (r.updatedAt > existing.updatedAt) {
            dao.insertEmi(row.copy(id = existing.id))
        }
    }

    companion object {
        private const val POLL_INTERVAL_MS = 3_000L
    }
}
