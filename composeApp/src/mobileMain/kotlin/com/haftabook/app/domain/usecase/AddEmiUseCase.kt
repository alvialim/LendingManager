package com.haftabook.app.domain.usecase

import com.haftabook.app.data.local.entity.EmiEntity
import com.haftabook.app.data.repository.EmiRepository
import com.haftabook.app.data.repository.LoanRepository
import com.haftabook.app.util.currentTimeMillis
import com.haftabook.app.utils.NumberHelper

/**
 * WHAT: Add EMI payment
 * WHERE: Domain Layer (Business Logic)
 * WHY: Validation and update loan remaining amount
 *
 * SOLID: Single Responsibility
 * - Only adds EMI
 * - Updates loan remaining
 */
class AddEmiUseCase(
    private val emiRepository: EmiRepository,
    private val loanRepository: LoanRepository
) {
    suspend fun execute(
        loanId: Long,
        amount: Long,
        emiDate: Long
    ): Result<Long> {
        // Validation
        if (amount <= 0) {
            return Result.failure(Exception("Amount must be greater than 0"))
        }

        // Get loan to check remaining amount
        val loan = loanRepository.getLoan(loanId)
            ?: return Result.failure(Exception("Loan not found"))

        if (amount > loan.remainingAmount) {
            return Result.failure(
                Exception(
                    "Amount cannot be more than remaining: ₹${NumberHelper.formatMoney(loan.remainingAmount)}"
                )
            )
        }

        // Get next EMI number
        val emiNumber = emiRepository.getNextEmiNumber(loanId)

        // Create EMI entity
        val emi = EmiEntity(
            loanId = loanId,
            emiNumber = emiNumber,
            emiAmount = amount,
            emiDate = emiDate,
            createdAt = currentTimeMillis()
        )

        // Save EMI
        val emiId = emiRepository.addEmi(emi)

        // Update loan remaining amount (Business Logic)
        val newRemaining = loan.remainingAmount - amount
        loanRepository.updateRemaining(loanId, newRemaining)

        return Result.success(emiId)
    }
}
