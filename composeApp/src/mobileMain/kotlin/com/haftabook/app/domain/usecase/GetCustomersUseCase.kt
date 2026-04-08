package com.haftabook.app.domain.usecase

import com.haftabook.app.data.local.model.CustomerWithTotals
import com.haftabook.app.data.repository.CustomerRepository
import com.haftabook.app.domain.model.Customer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * WHAT: Get customers with calculations
 * WHERE: Domain Layer (Clean Architecture)
 * WHY: Business logic stays separate from UI
 */
class GetCustomersUseCase(
    private val repository: CustomerRepository
) {
    /**
     * All customers (mapped to domain); emits on every relevant DB change.
     * Prefer this for UI that combines with tab/search — avoids `flatMapLatest` missing Room emissions.
     */
    fun observeAllCustomers(): Flow<List<Customer>> {
        return repository.observeAllCustomersWithTotals()
            .map { results -> results.map(::mapRowToCustomer) }
            .flowOn(Dispatchers.Default)
    }

    /** Immediate read after insert/update when the Room [Flow] has not emitted yet. */
    suspend fun loadAllCustomersSnapshot(): List<Customer> = withContext(Dispatchers.IO) {
        repository.getCustomersWithTotalsSnapshot().map(::mapRowToCustomer)
    }

    // Get customers by type with all calculations
    fun execute(type: String): Flow<List<Customer>> {
        return repository.getCustomersWithTotals(type)
            .map { results -> results.map(::mapRowToCustomer) }
            .flowOn(Dispatchers.Default)
    }

    private fun mapRowToCustomer(result: CustomerWithTotals): Customer {
        val entity = result.customer
        val totalGiven = result.totalGiven ?: 0L
        val totalDue = result.totalDue ?: 0L
        return Customer(
            id = entity.id,
            name = entity.name,
            mobile = entity.mobile,
            loanType = entity.loanType,
            photoPath = entity.photoPath,
            totalGiven = totalGiven,
            totalPaid = totalGiven - totalDue,
            totalDue = totalDue,
            totalLoans = result.totalLoans
        )
    }
}
