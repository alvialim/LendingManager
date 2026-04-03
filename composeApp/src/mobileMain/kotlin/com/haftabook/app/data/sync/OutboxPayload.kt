package com.haftabook.app.data.sync

import kotlinx.serialization.Serializable

@Serializable
data class CustomerDeletePayload(
    val remoteId: String,
    val firestoreCustomerDocId: String = ""
)

@Serializable
data class LoanDeletePayload(
    val remoteId: String,
    val firestoreCustomerDocId: String = ""
)

@Serializable
data class EmiDeletePayload(
    val remoteId: String,
    val firestoreCustomerDocId: String = "",
    val loanRemoteId: String = ""
)
