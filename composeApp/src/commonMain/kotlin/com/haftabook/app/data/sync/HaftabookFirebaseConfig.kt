package com.haftabook.app.data.sync

/**
 * Realtime Database URL from Firebase Console → Realtime Database → Data (no trailing slash).
 *
 * Top-level constants (not [object]) avoid a Kotlin 2.1 K2/JS compiler crash on `object` in commonMain.
 */
const val HAFTABOOK_REALTIME_DATABASE_URL: String = "https://haftabookkmp-default-rtdb.firebaseio.com"

/**
 * Cloud Firestore layout (nested under each customer):
 * `customers/{customerDocId}` — customer fields (see [firestoreCustomerDocId])
 * `customers/{customerDocId}/loans/{loanUuid}` — loan (id matches Realtime DB UUID)
 * `customers/{customerDocId}/loans/{loanUuid}/emis/{emiUuid}` — EMI
 *
 * Legacy top-level `loans` / `emis` collections are only used when deleting old outbox rows
 * that lack nested path fields in the delete payload.
 */
const val FIRESTORE_COLLECTION_CUSTOMERS: String = "customers"
const val FIRESTORE_SUBCOLLECTION_LOANS: String = "loans"
const val FIRESTORE_SUBCOLLECTION_EMIS: String = "emis"
/** Legacy flat layout; kept for backward-compatible Firestore deletes. */
const val FIRESTORE_COLLECTION_LOANS: String = "loans"
/** Legacy flat layout; kept for backward-compatible Firestore deletes. */
const val FIRESTORE_COLLECTION_EMIS: String = "emis"
