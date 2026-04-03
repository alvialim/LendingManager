package com.haftabook.app.domain.usecase



import com.haftabook.app.data.repository.CustomerRepository
import com.haftabook.app.data.repository.LoanRepository
import com.haftabook.app.data.repository.EmiRepository
import com.haftabook.app.domain.model.Customer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

/**
 * WHAT: Get customer with all calculations
 * WHERE: Domain Layer (Business Logic)
 * WHY: Calculate totals from all loans and EMIs
 *
 * SOLID: Single Responsibility
 * - Only gets customer details with calculations
 *
 * Clean Architecture:
 * - Domain layer uses Data layer (repositories)
 * - Presentation layer uses this Use Case
 */
class GetCustomerDetailsUseCase(
    private val customerRepository: CustomerRepository,
    private val loanRepository: LoanRepository,
    private val emiRepository: EmiRepository
) {
    suspend fun execute(customerId: Long): Customer? = withContext(Dispatchers.IO) {
        val customerEntity = customerRepository.getCustomer(customerId)
            ?: return@withContext null

        val loans = loanRepository.getLoansForCustomer(customerId).first()

        var totalGiven = 0L
        var totalPaid = 0L
        var totalDue = 0L

        loans.forEach { loan ->
            totalGiven += loan.loanAmount
            totalDue += loan.remainingAmount
            val paidForLoan = emiRepository.getTotalPaid(loan.id)
            totalPaid += paidForLoan
        }

        Customer(
            id = customerEntity.id,
            name = customerEntity.name,
            mobile = customerEntity.mobile,
            loanType = customerEntity.loanType,
            totalGiven = totalGiven,
            totalPaid = totalPaid,
            totalDue = totalDue,
            totalLoans = loans.size
        )
    }
}

/**
 * ARCHITECTURE EXPLANATION:
 *
 * Why separate Use Case?
 * ✅ Business logic in one place
 * ✅ Easy to test calculations
 * ✅ ViewModel doesn't need to know calculation logic
 * ✅ If calculation changes, only this file changes
 */