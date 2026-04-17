package com.haftabook.app.domain.usecase

import com.haftabook.app.data.local.entity.EmiEntity
import com.haftabook.app.data.repository.EmiRepository
import com.haftabook.app.data.repository.LoanRepository
import com.haftabook.app.util.currentTimeMillis
import com.haftabook.app.utils.DateHelper
import kotlin.math.min

/**
 * Marks a scheduled EMI slot (1..totalEmis) as paid with the standard EMI amount
 * (or remaining balance if lower).
 */
class MarkEmiSlotPaidUseCase(
    private val emiRepository: EmiRepository,
    private val loanRepository: LoanRepository,
) {
    /** On success returns the amount recorded for this EMI (for notifications). */
    suspend fun execute(loanId: Long, emiNumber: Int, loanType: String): Result<Long> {
        val loan = loanRepository.getLoan(loanId)
            ?: return Result.failure(Exception("Loan not found"))

        if (emiNumber < 1 || emiNumber > loan.totalEmis) {
            return Result.failure(Exception("Invalid EMI number"))
        }

        if (emiRepository.getEmiByLoanAndNumber(loanId, emiNumber) != null) {
            return Result.failure(Exception("This EMI is already marked paid"))
        }

        val isMonthly = loanType == "MONTHLY"
        val payAmount = if (isMonthly) {
            loan.emiAmount
        } else {
            min(loan.emiAmount, loan.remainingAmount)
        }
        if (payAmount <= 0L) return Result.failure(Exception("Nothing left to pay on this loan"))

        val dueDateForSlot =
            DateHelper.scheduledEmiDate(loan.emiStartDate, emiNumber, loanType)

        val emi = EmiEntity(
            loanId = loanId,
            emiNumber = emiNumber,
            emiAmount = payAmount,
            emiDate = dueDateForSlot,
            createdAt = currentTimeMillis(),
        )

        emiRepository.addEmi(emi)
        if (!isMonthly) {
            val newRemaining = loan.remainingAmount - payAmount
            loanRepository.updateRemaining(loanId, newRemaining)
        }

        return Result.success(payAmount)
    }
}
