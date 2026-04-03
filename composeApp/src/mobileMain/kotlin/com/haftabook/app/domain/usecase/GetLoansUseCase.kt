package com.haftabook.app.domain.usecase

import com.haftabook.app.data.repository.LoanRepository
import com.haftabook.app.domain.model.Loan
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

/**
 * WHAT: Get loans for a customer
 * WHERE: Domain Layer
 */
class GetLoansUseCase(
    private val loanRepository: LoanRepository
) {
    fun execute(customerId: Long): Flow<List<Loan>> {
        return loanRepository.getLoansForCustomer(customerId).map { loanEntities ->
            loanEntities.map { loanEntity ->
                Loan(
                    id = loanEntity.id,
                    customerId = loanEntity.customerId,
                    loanNumber = loanEntity.loanNumber,
                    loanAmount = loanEntity.loanAmount,
                    emiAmount = loanEntity.emiAmount,      // Added EMI amount mapping
                    loanStartDate = loanEntity.loanStartDate,
                    emiStartDate = loanEntity.emiStartDate,
                    totalEmis = loanEntity.totalEmis,
                    lastEmiDate = loanEntity.lastEmiDate,
                    remainingAmount = loanEntity.remainingAmount
                )
            }
        }.flowOn(Dispatchers.Default)
    }
}
