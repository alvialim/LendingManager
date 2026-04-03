package com.haftabook.app.di

import com.haftabook.app.data.AppDatabase
import com.haftabook.app.data.repository.CustomerRepository
import com.haftabook.app.data.repository.EmiRepository
import com.haftabook.app.data.repository.LoanRepository
import com.haftabook.app.domain.usecase.*
import com.haftabook.app.network.NetworkMonitor
import kotlinx.coroutines.flow.Flow

/**
 * Shared dependency container for all platforms.
 * Room is the on-device cache; [com.haftabook.app.data.sync.FirebaseSyncEngine] syncs outbox writes to
 * Firebase Realtime Database (live listeners) and Cloud Firestore (structured documents).
 */
class AppContainer(
    val database: AppDatabase,
    val networkMonitor: NetworkMonitor,
    private val onRequestSync: suspend () -> Unit = {}
) {

    /** Pending cloud operations (upload) not yet pushed to Firebase. */
    val pendingSyncCount: Flow<Int> = database.syncOutboxDao().observePendingCount()

    suspend fun requestSyncNow() = onRequestSync()

    // Repositories
    val customerRepository = CustomerRepository(database)
    val loanRepository = LoanRepository(database)
    val emiRepository = EmiRepository(database)

    // Use Cases
    val getCustomersUseCase = GetCustomersUseCase(customerRepository)
    val addCustomerUseCase = AddCustomerUseCase(customerRepository)
    val deleteCustomerUseCase = DeleteCustomerUseCase(customerRepository)

    val getCustomerDetailsUseCase = GetCustomerDetailsUseCase(
        customerRepository,
        loanRepository,
        emiRepository
    )
    val getLoansUseCase = GetLoansUseCase(loanRepository)
    val getEmisUseCase = GetEmisUseCase(emiRepository)
    val addLoanUseCase = AddLoanUseCase(loanRepository)
    val addEmiUseCase = AddEmiUseCase(emiRepository, loanRepository)
    val deleteLoanUseCase = DeleteLoanUseCase(loanRepository)

    val getAnalyticsUseCase = GetAnalyticsUseCase(loanRepository, emiRepository, customerRepository)
}
