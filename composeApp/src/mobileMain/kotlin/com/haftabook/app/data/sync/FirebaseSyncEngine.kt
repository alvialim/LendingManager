package com.haftabook.app.data.sync

import com.haftabook.app.data.AppDatabase
import com.haftabook.app.data.local.entity.CustomerEntity
import com.haftabook.app.data.local.entity.EmiEntity
import com.haftabook.app.data.local.entity.LoanEntity
import com.haftabook.app.data.remote.CustomerRemote
import com.haftabook.app.data.remote.EmiRemote
import com.haftabook.app.data.remote.LoanRemote
import com.haftabook.app.network.NetworkMonitor
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.database.database
import dev.gitlive.firebase.database.DataSnapshot
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.retry
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.serialization.json.Json

/**
 * Cloud sync for Room outbox:
 * - **Realtime Database** — live listeners merge into Room (flat `customers` / `loans` / `emis` keys = UUIDs).
 * - **Cloud Firestore** — nested: `customers/{customerDocId}/loans/{loanUuid}/emis/{emiUuid}` (see [HaftabookFirebaseConfig]).
 *
 * Deletes are enqueued first, then local rows are removed. While a DELETE is still in the outbox, merges from
 * RTDB **skip** that `remoteId` so the listener cannot resurrect rows the user already deleted locally.
 *
 * Rules for both products must allow your app (see `database.rules.json` and `firestore.rules`).
 *
 * **Offline-first:** Local writes go to Room + outbox immediately. Outbox rows are pushed only while
 * [NetworkMonitor.isOnline] is true, so failed writes are not spammed when offline.
 */
class FirebaseSyncEngine(
    private val db: AppDatabase,
    private val networkMonitor: NetworkMonitor,
    /** Realtime Database URL from Firebase Console (Data tab), without a trailing slash. */
    private val firebaseDatabaseUrl: String = HAFTABOOK_REALTIME_DATABASE_URL,
    private val json: Json = Json { ignoreUnknownKeys = true; encodeDefaults = true }
) {
    // Persistence must be enabled before any [reference] call (GitLive / Android RTDB).
    private val firebaseDatabase = run {
        val db = Firebase.database(firebaseDatabaseUrl)
        runCatching { db.setPersistenceEnabled(true) }
            .onFailure { log("setPersistenceEnabled: ${it.message}") }
        db
    }
    private val customersRef = firebaseDatabase.reference(FIRESTORE_COLLECTION_CUSTOMERS)
    private val loansRef = firebaseDatabase.reference(FIRESTORE_COLLECTION_LOANS)
    private val emisRef = firebaseDatabase.reference(FIRESTORE_COLLECTION_EMIS)

    private val firestore = Firebase.firestore

    private data class PendingDeleteRemoteIds(
        val customers: Set<String>,
        val loans: Set<String>,
        val emis: Set<String>
    )

    private val customersState = MutableStateFlow<Map<String, CustomerRemote>>(emptyMap())
    private val loansState = MutableStateFlow<Map<String, LoanRemote>>(emptyMap())
    private val emisState = MutableStateFlow<Map<String, EmiRemote>>(emptyMap())

    fun start(scope: CoroutineScope) {
        networkMonitor.start()
        scope.launch { observeRemoteChanges() }
        scope.launch {
            networkMonitor.isOnline.collectLatest { online ->
                if (!online) return@collectLatest
                while (isActive && networkMonitor.isOnline.value) {
                    val hadRows = drainOutboxBatch()
                    delay(if (hadRows) DRAIN_AGAIN_MS else DRAIN_IDLE_MS)
                }
            }
        }
    }

    /**
     * Flushes all pending outbox rows to Firebase (pull-to-refresh). No-op when offline.
     */
    suspend fun flushPendingSyncToCloud() {
        if (!networkMonitor.isOnline.value) return
        while (networkMonitor.isOnline.value) {
            val hadRows = drainOutboxBatch()
            if (!hadRows) break
        }
    }

    /**
     * Pushes pending outbox rows to RTDB. Returns true if there was work (caller may retry soon).
     * Failures are logged and stored via [SyncOutboxDao.markFailure] so they are not silently dropped.
     */
    private suspend fun drainOutboxBatch(): Boolean {
        val rows = db.syncOutboxDao().peek(20)
        if (rows.isEmpty()) return false
        for (row in rows) {
            val result = runCatching {
                when (row.entityType) {
                    SyncEntityType.CUSTOMER -> when (row.operation) {
                        SyncOperation.UPSERT -> {
                            val payload = json.decodeFromString(CustomerRemote.serializer(), row.payloadJson)
                            customersRef.child(payload.id).setValue(payload)
                            val docId = firestoreCustomerDocId(payload.name, payload.mobile, payload.createdDate)
                            firestore.collection(FIRESTORE_COLLECTION_CUSTOMERS).document(docId).set(payload)
                        }
                        SyncOperation.DELETE -> {
                            val p = json.decodeFromString(CustomerDeletePayload.serializer(), row.payloadJson)
                            customersRef.child(p.remoteId).removeValue()
                            if (p.firestoreCustomerDocId.isNotEmpty()) {
                                firestore.collection(FIRESTORE_COLLECTION_CUSTOMERS).document(p.firestoreCustomerDocId).delete()
                            }
                            // Legacy flat doc (UUID) or nested-only installs; no-op if missing
                            firestore.collection(FIRESTORE_COLLECTION_CUSTOMERS).document(p.remoteId).delete()
                        }
                        else -> error("bad op")
                    }
                    SyncEntityType.LOAN -> when (row.operation) {
                        SyncOperation.UPSERT -> {
                            val payload = json.decodeFromString(LoanRemote.serializer(), row.payloadJson)
                            loansRef.child(payload.id).setValue(payload)
                            db.customerDao().getCustomerByRemoteId(payload.customerId)?.let { customer ->
                                val cid = firestoreCustomerDocId(customer.name, customer.mobile, customer.createdDate)
                                firestore.collection(FIRESTORE_COLLECTION_CUSTOMERS).document(cid)
                                    .collection(FIRESTORE_SUBCOLLECTION_LOANS).document(payload.id)
                                    .set(payload)
                            }
                        }
                        SyncOperation.DELETE -> {
                            val p = json.decodeFromString(LoanDeletePayload.serializer(), row.payloadJson)
                            loansRef.child(p.remoteId).removeValue()
                            if (p.firestoreCustomerDocId.isNotEmpty()) {
                                firestore.collection(FIRESTORE_COLLECTION_CUSTOMERS).document(p.firestoreCustomerDocId)
                                    .collection(FIRESTORE_SUBCOLLECTION_LOANS).document(p.remoteId).delete()
                            }
                            firestore.collection(FIRESTORE_COLLECTION_LOANS).document(p.remoteId).delete()
                        }
                        else -> error("bad op")
                    }
                    SyncEntityType.EMI -> when (row.operation) {
                        SyncOperation.UPSERT -> {
                            val payload = json.decodeFromString(EmiRemote.serializer(), row.payloadJson)
                            emisRef.child(payload.id).setValue(payload)
                            val loan = db.loanDao().getLoanByRemoteId(payload.loanId)
                            val customer = loan?.let { db.customerDao().getCustomerById(it.customerId) }
                            if (loan != null && customer != null) {
                                val cid = firestoreCustomerDocId(customer.name, customer.mobile, customer.createdDate)
                                firestore.collection(FIRESTORE_COLLECTION_CUSTOMERS).document(cid)
                                    .collection(FIRESTORE_SUBCOLLECTION_LOANS).document(payload.loanId)
                                    .collection(FIRESTORE_SUBCOLLECTION_EMIS).document(payload.id)
                                    .set(payload)
                            }
                        }
                        SyncOperation.DELETE -> {
                            val p = json.decodeFromString(EmiDeletePayload.serializer(), row.payloadJson)
                            emisRef.child(p.remoteId).removeValue()
                            if (p.firestoreCustomerDocId.isNotEmpty() && p.loanRemoteId.isNotEmpty()) {
                                firestore.collection(FIRESTORE_COLLECTION_CUSTOMERS).document(p.firestoreCustomerDocId)
                                    .collection(FIRESTORE_SUBCOLLECTION_LOANS).document(p.loanRemoteId)
                                    .collection(FIRESTORE_SUBCOLLECTION_EMIS).document(p.remoteId).delete()
                            }
                            firestore.collection(FIRESTORE_COLLECTION_EMIS).document(p.remoteId).delete()
                        }
                        else -> error("bad op")
                    }
                    else -> error("unknown entity")
                }
            }
            if (result.isSuccess) {
                db.syncOutboxDao().deleteById(row.id)
                log("Cloud write OK (RTDB+Firestore) entity=${row.entityType} op=${row.operation} outboxId=${row.id}")
            } else {
                val msg = result.exceptionOrNull()?.message ?: result.toString()
                val safe = msg.take(400)
                db.syncOutboxDao().markFailure(row.id, safe)
                log("Cloud write FAILED (RTDB and/or Firestore) entity=${row.entityType} outboxId=${row.id}: $safe")
            }
        }
        return true
    }

    private fun log(message: String) {
        println("[HaftaBookSync] $message")
        SyncDiagnostics.noteLog(message)
    }

    /**
     * Retries after permission/network errors so RTDB [valueEvents] does not crash the process.
     * You must still allow read/write in Firebase Console → Realtime Database → Rules (see database.rules.json).
     */
    private fun retryingValueEvents(flow: Flow<DataSnapshot>) = flow.retry { cause ->
        if (cause is CancellationException) return@retry false
        SyncDiagnostics.noteError("RTDB listener error: ${cause.message ?: cause::class.simpleName}")
        delay(RETRY_MS)
        true
    }

    private suspend fun observeRemoteChanges() = supervisorScope {
        launch {
            retryingValueEvents(customersRef.valueEvents).collectLatest { snapshot ->
                customersState.value = snapshot.value<Map<String, CustomerRemote>?>() ?: emptyMap()
            }
        }
        launch {
            retryingValueEvents(loansRef.valueEvents).collectLatest { snapshot ->
                loansState.value = snapshot.value<Map<String, LoanRemote>?>() ?: emptyMap()
            }
        }
        launch {
            retryingValueEvents(emisRef.valueEvents).collectLatest { snapshot ->
                emisState.value = snapshot.value<Map<String, EmiRemote>?>() ?: emptyMap()
            }
        }
        launch {
            combine(
                customersState,
                loansState,
                emisState
            ) { c: Map<String, CustomerRemote>, l: Map<String, LoanRemote>, e: Map<String, EmiRemote> ->
                Triple(c, l, e)
            }
                .collectLatest { (c, l, e) ->
                    val pendingDeletes = loadPendingDeleteRemoteIds()
                    c.values.forEach { remote ->
                        if (remote.id !in pendingDeletes.customers) mergeCustomer(remote)
                    }
                    l.values.forEach { remote ->
                        if (remote.id !in pendingDeletes.loans) mergeLoan(remote)
                    }
                    e.values.forEach { remote ->
                        if (remote.id !in pendingDeletes.emis) mergeEmi(remote)
                    }
                }
        }
    }

    private suspend fun loadPendingDeleteRemoteIds(): PendingDeleteRemoteIds {
        val customers = mutableSetOf<String>()
        val loans = mutableSetOf<String>()
        val emis = mutableSetOf<String>()
        for (row in db.syncOutboxDao().getPendingDeletes()) {
            runCatching {
                when (row.entityType) {
                    SyncEntityType.CUSTOMER -> customers.add(
                        json.decodeFromString(CustomerDeletePayload.serializer(), row.payloadJson).remoteId
                    )
                    SyncEntityType.LOAN -> loans.add(
                        json.decodeFromString(LoanDeletePayload.serializer(), row.payloadJson).remoteId
                    )
                    SyncEntityType.EMI -> emis.add(
                        json.decodeFromString(EmiDeletePayload.serializer(), row.payloadJson).remoteId
                    )
                }
            }
        }
        return PendingDeleteRemoteIds(customers, loans, emis)
    }

    private companion object {
        private const val RETRY_MS = 10_000L
        private const val DRAIN_AGAIN_MS = 300L
        private const val DRAIN_IDLE_MS = 1000L
    }

    private suspend fun mergeCustomer(r: CustomerRemote) {
        val dao = db.customerDao()
        val existing = dao.getCustomerByRemoteId(r.id)
        if (existing == null || r.updatedAt > existing.updatedAt) {
            dao.insertCustomer(
                CustomerEntity(
                    id = existing?.id ?: 0,
                    remoteId = r.id,
                    updatedAt = r.updatedAt,
                    name = r.name,
                    mobile = r.mobile,
                    loanType = r.loanType,
                    createdDate = r.createdDate,
                    // Local-only field; never comes from remote.
                    photoPath = existing?.photoPath
                )
            )
        }
    }

    private suspend fun mergeLoan(r: LoanRemote) {
        val customer = db.customerDao().getCustomerByRemoteId(r.customerId) ?: return
        val dao = db.loanDao()
        val existing = dao.getLoanByRemoteId(r.id)
        if (existing == null || r.updatedAt > existing.updatedAt) {
            dao.insertLoan(
                LoanEntity(
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
            )
        }
    }

    private suspend fun mergeEmi(r: EmiRemote) {
        val loan = db.loanDao().getLoanByRemoteId(r.loanId) ?: return
        val dao = db.emiDao()
        val existing = dao.getEmiByRemoteId(r.id)
        if (existing == null || r.updatedAt > existing.updatedAt) {
            dao.insertEmi(
                EmiEntity(
                    id = existing?.id ?: 0,
                    remoteId = r.id,
                    updatedAt = r.updatedAt,
                    loanId = loan.id,
                    emiNumber = r.emiNumber,
                    emiAmount = r.emiAmount,
                    emiDate = r.emiDate,
                    createdAt = r.createdAt
                )
            )
        }
    }
}
