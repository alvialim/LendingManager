package com.haftabook.app.data.sync

import com.haftabook.app.data.AppDatabase
import com.haftabook.app.data.local.entity.SyncOutboxEntity
import com.haftabook.app.data.remote.CustomerRemote
import com.haftabook.app.data.remote.EmiRemote
import com.haftabook.app.data.remote.LoanRemote
import com.haftabook.app.data.withTransactionCompat
import com.haftabook.app.util.newUuidV4
import kotlinx.serialization.json.Json

private val outboxJson = Json { ignoreUnknownKeys = true; encodeDefaults = true }

/**
 * Runs inside [withTransactionCompat]. Do not nest another write transaction.
 */
suspend fun AppDatabase.assignCustomerRemoteIdInCurrentTx(customerId: Long): String {
    val c = customerDao().getCustomerById(customerId) ?: error("customer not found")
    if (c.remoteId != null) return c.remoteId!!
    val rid = newUuidV4()
    val now = System.currentTimeMillis()
    val u = c.copy(remoteId = rid, updatedAt = now)
    customerDao().insertCustomer(u)
    val inserted = customerDao().getCustomerById(customerId)!!
    val payload = outboxJson.encodeToString(
        CustomerRemote.serializer(),
        inserted.toCustomerRemote()
    )
    syncOutboxDao().insert(
        SyncOutboxEntity(
            entityType = SyncEntityType.CUSTOMER,
            operation = SyncOperation.UPSERT,
            payloadJson = payload,
            createdAtEpochMs = now
        )
    )
    return rid
}

suspend fun AppDatabase.ensureCustomerRemoteId(customerId: Long): String = withTransactionCompat {
    assignCustomerRemoteIdInCurrentTx(customerId)
}

/**
 * Runs inside [withTransactionCompat]. Do not nest another write transaction.
 */
suspend fun AppDatabase.assignLoanRemoteIdInCurrentTx(loanId: Long): String {
    val loan = loanDao().getLoanById(loanId) ?: error("loan not found")
    if (loan.remoteId != null) return loan.remoteId!!
    val customerRid = assignCustomerRemoteIdInCurrentTx(loan.customerId)
    val rid = newUuidV4()
    val now = System.currentTimeMillis()
    val u = loan.copy(remoteId = rid, updatedAt = now)
    loanDao().insertLoan(u)
    val inserted = loanDao().getLoanById(loanId)!!
    val payload = outboxJson.encodeToString(
        LoanRemote.serializer(),
        inserted.toLoanRemote(customerRid)
    )
    syncOutboxDao().insert(
        SyncOutboxEntity(
            entityType = SyncEntityType.LOAN,
            operation = SyncOperation.UPSERT,
            payloadJson = payload,
            createdAtEpochMs = now
        )
    )
    return rid
}

suspend fun AppDatabase.ensureLoanRemoteId(loanId: Long): String = withTransactionCompat {
    assignLoanRemoteIdInCurrentTx(loanId)
}

/**
 * Runs inside [withTransactionCompat]. Do not nest another write transaction.
 */
suspend fun AppDatabase.assignEmiRemoteIdInCurrentTx(emiId: Long): String {
    val emi = emiDao().getEmiById(emiId) ?: error("emi not found")
    if (emi.remoteId != null) return emi.remoteId!!
    val loanRid = assignLoanRemoteIdInCurrentTx(emi.loanId)
    val rid = newUuidV4()
    val now = System.currentTimeMillis()
    val u = emi.copy(remoteId = rid, updatedAt = now)
    emiDao().insertEmi(u)
    val inserted = emiDao().getEmiById(emiId)!!
    val payload = outboxJson.encodeToString(
        EmiRemote.serializer(),
        inserted.toEmiRemote(loanRid)
    )
    syncOutboxDao().insert(
        SyncOutboxEntity(
            entityType = SyncEntityType.EMI,
            operation = SyncOperation.UPSERT,
            payloadJson = payload,
            createdAtEpochMs = now
        )
    )
    return rid
}

suspend fun AppDatabase.ensureEmiRemoteId(emiId: Long): String = withTransactionCompat {
    assignEmiRemoteIdInCurrentTx(emiId)
}

/**
 * Push latest loan state to Firebase after local updates (e.g. remaining balance).
 */
suspend fun AppDatabase.enqueueLoanUpsertAfterChange(loanId: Long) = withTransactionCompat {
    val loan = loanDao().getLoanById(loanId) ?: return@withTransactionCompat
    if (loan.remoteId == null) {
        assignLoanRemoteIdInCurrentTx(loanId)
        return@withTransactionCompat
    }
    val customerRid = assignCustomerRemoteIdInCurrentTx(loan.customerId)
    val payload = outboxJson.encodeToString(
        LoanRemote.serializer(),
        loan.toLoanRemote(customerRid)
    )
    syncOutboxDao().insert(
        SyncOutboxEntity(
            entityType = SyncEntityType.LOAN,
            operation = SyncOperation.UPSERT,
            payloadJson = payload,
            createdAtEpochMs = System.currentTimeMillis()
        )
    )
}

suspend fun AppDatabase.enqueueEmiDelete(
    remoteId: String,
    firestoreCustomerDocId: String,
    loanRemoteId: String
) {
    syncOutboxDao().insert(
        SyncOutboxEntity(
            entityType = SyncEntityType.EMI,
            operation = SyncOperation.DELETE,
            payloadJson = outboxJson.encodeToString(
                EmiDeletePayload.serializer(),
                EmiDeletePayload(remoteId, firestoreCustomerDocId, loanRemoteId)
            ),
            createdAtEpochMs = System.currentTimeMillis()
        )
    )
}

suspend fun AppDatabase.enqueueLoanDelete(remoteId: String, firestoreCustomerDocId: String) {
    syncOutboxDao().insert(
        SyncOutboxEntity(
            entityType = SyncEntityType.LOAN,
            operation = SyncOperation.DELETE,
            payloadJson = outboxJson.encodeToString(
                LoanDeletePayload.serializer(),
                LoanDeletePayload(remoteId, firestoreCustomerDocId)
            ),
            createdAtEpochMs = System.currentTimeMillis()
        )
    )
}

suspend fun AppDatabase.enqueueCustomerDelete(remoteId: String, firestoreCustomerDocId: String) {
    syncOutboxDao().insert(
        SyncOutboxEntity(
            entityType = SyncEntityType.CUSTOMER,
            operation = SyncOperation.DELETE,
            payloadJson = outboxJson.encodeToString(
                CustomerDeletePayload.serializer(),
                CustomerDeletePayload(remoteId, firestoreCustomerDocId)
            ),
            createdAtEpochMs = System.currentTimeMillis()
        )
    )
}
