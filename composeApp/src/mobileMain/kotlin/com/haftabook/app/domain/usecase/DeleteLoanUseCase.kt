package com.haftabook.app.domain.usecase

import com.haftabook.app.data.repository.LoanRepository

/**
 * WHAT: Delete a specific loan and its EMIs
 * WHERE: Domain Layer
 */
class DeleteLoanUseCase(
    private val loanRepository: LoanRepository
) {
    suspend fun execute(loanId: Long) {
        loanRepository.deleteLoanWithSync(loanId)
    }
}
