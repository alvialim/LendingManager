package com.haftabook.app.domain.usecase

import com.haftabook.app.data.local.entity.LoanEntity
import com.haftabook.app.data.repository.LoanRepository
import com.haftabook.app.utils.DateHelper
import kotlin.math.ceil

/**
 * WHAT: Add new loan
 * WHERE: Domain Layer (Business Logic)
 */
class AddLoanUseCase(
    private val loanRepository: LoanRepository
) {
    suspend fun execute(
        customerId: Long,
        loanType: String,
        amount: Long,
        loanStartDate: Long,
        emiStartDate: Long,
        totalEmis: Int
    ): Result<Long> {
        // Validation
        if (amount <= 0) return Result.failure(Exception("Amount must be > 0"))
        if (totalEmis <= 0) return Result.failure(Exception("Total EMIs must be > 0"))
        if (emiStartDate < loanStartDate) return Result.failure(Exception("EMI start date cannot be before loan start date"))

        // Calculate EMI Amount (Business Logic)
        // Using ceiling to ensure the total amount is covered
        val emiAmount = ceil(amount.toDouble() / totalEmis).toLong()

        // Calculate last EMI date
        val lastEmiDate = when (loanType) {
            "DAILY" -> DateHelper.addDays(emiStartDate, totalEmis - 1)
            "MONTHLY" -> DateHelper.addMonths(emiStartDate, totalEmis - 1)
            else -> emiStartDate
        }

        val loanNumber = loanRepository.getNextLoanNumber(customerId)

        val loan = LoanEntity(
            customerId = customerId,
            loanNumber = loanNumber,
            loanAmount = amount,
            emiAmount = emiAmount,       // Saved EMI amount
            loanStartDate = loanStartDate,
            emiStartDate = emiStartDate,
            totalEmis = totalEmis,
            lastEmiDate = lastEmiDate,
            remainingAmount = amount
        )

        val loanId = loanRepository.addLoan(loan)
        
        // Task 2 Placeholder: Trigger notification here if needed
        // triggerLoanAddedNotification(loanId)

        return Result.success(loanId)
    }
}
