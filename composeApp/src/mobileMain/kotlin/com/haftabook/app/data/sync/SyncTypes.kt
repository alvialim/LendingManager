package com.haftabook.app.data.sync

object SyncEntityType {
    const val CUSTOMER = "customer"
    const val LOAN = "loan"
    const val EMI = "emi"
}

object SyncOperation {
    const val UPSERT = "UPSERT"
    const val DELETE = "DELETE"
}

object SyncStateKeys {
    const val LAST_PULL_CUSTOMERS = "last_pull_customers"
    const val LAST_PULL_LOANS = "last_pull_loans"
    const val LAST_PULL_EMIS = "last_pull_emis"
}
