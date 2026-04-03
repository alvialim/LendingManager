package com.haftabook.app.data.repository

import com.haftabook.app.data.AppDatabase
import com.haftabook.app.data.local.entity.LoanEntity
import com.haftabook.app.data.sync.enqueueEmiDelete
import com.haftabook.app.data.sync.enqueueLoanDelete
import com.haftabook.app.data.sync.enqueueLoanUpsertAfterChange
import com.haftabook.app.data.sync.firestoreCustomerDocId
import com.haftabook.app.data.sync.ensureLoanRemoteId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class LoanRepository(
    private val db: AppDatabase
) {
    private val loanDao get() = db.loanDao()
    private val emiDao get() = db.emiDao()
    private val customerDao get() = db.customerDao()

    fun getLoansForCustomer(customerId: Long): Flow<List<LoanEntity>> =
        loanDao.getLoansForCustomer(customerId)

    suspend fun getLoan(loanId: Long): LoanEntity? = loanDao.getLoanById(loanId)

    suspend fun addLoan(loan: LoanEntity): Long {
        val now = System.currentTimeMillis()
        val row = loan.copy(updatedAt = now)
        val id = loanDao.insertLoan(row)
        db.ensureLoanRemoteId(id)
        return id
    }

    suspend fun getNextLoanNumber(customerId: Long): Int =
        loanDao.getNextLoanNumber(customerId)

    suspend fun updateRemaining(loanId: Long, amount: Long) {
        val loan = loanDao.getLoanById(loanId) ?: return
        val now = System.currentTimeMillis()
        loanDao.insertLoan(loan.copy(remainingAmount = amount, updatedAt = now))
        db.enqueueLoanUpsertAfterChange(loanId)
    }

    suspend fun getTotalGiven(customerId: Long): Long =
        loanDao.getTotalLoanForCustomer(customerId).first() ?: 0L

    suspend fun getTotalDue(customerId: Long): Long =
        loanDao.getTotalRemainingForCustomer(customerId).first() ?: 0L

    suspend fun deleteLoanWithSync(loanId: Long) {
        val loan = loanDao.getLoanById(loanId) ?: return
        val customer = customerDao.getCustomerById(loan.customerId) ?: return
        val customerDocId = firestoreCustomerDocId(customer.name, customer.mobile, customer.createdDate)
        val emis = emiDao.getEmisForLoan(loanId).first()
        for (emi in emis) {
            emi.remoteId?.let { eid ->
                loan.remoteId?.let { lid -> db.enqueueEmiDelete(eid, customerDocId, lid) }
            }
        }
        loan.remoteId?.let { db.enqueueLoanDelete(it, customerDocId) }
        emiDao.deleteEmisForLoan(loanId)
        loanDao.deleteLoanById(loanId)
    }

    suspend fun deleteLoansByCustomer(customerId: Long) {
        loanDao.deleteLoansForCustomer(customerId)
    }

    suspend fun getAllLoans(): List<LoanEntity> = loanDao.getAllLoansList()
}
