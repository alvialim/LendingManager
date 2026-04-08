package com.haftabook.app.data.repository

import com.haftabook.app.data.AppDatabase
import com.haftabook.app.data.local.entity.CustomerEntity
import com.haftabook.app.data.local.model.CustomerWithTotals
import com.haftabook.app.data.sync.assignCustomerRemoteIdInCurrentTx
import com.haftabook.app.data.sync.enqueueCustomerDelete
import com.haftabook.app.data.sync.enqueueEmiDelete
import com.haftabook.app.data.sync.enqueueLoanDelete
import com.haftabook.app.data.sync.firestoreCustomerDocId
import com.haftabook.app.data.withTransactionCompat
import com.haftabook.app.util.currentTimeMillis
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class CustomerRepository(
    private val db: AppDatabase
) {
    private val customerDao get() = db.customerDao()
    private val loanDao get() = db.loanDao()
    private val emiDao get() = db.emiDao()

    /** Emits whenever `customers` or `loans` data changes (Room invalidation). */
    fun observeAllCustomersWithTotals(): Flow<List<CustomerWithTotals>> =
        customerDao.getCustomersWithTotals()

    suspend fun getCustomersWithTotalsSnapshot(): List<CustomerWithTotals> =
        customerDao.getCustomersWithTotalsSnapshot()

    fun getCustomersWithTotals(type: String): Flow<List<CustomerWithTotals>> {
        return customerDao.getCustomersWithTotals().map { list ->
            list.filter { it.customer.loanType == type }
        }
    }

    suspend fun addCustomer(customer: CustomerEntity): Long {
        return db.withTransactionCompat {
            val now = currentTimeMillis()
            val row = customer.copy(updatedAt = now)
            val id = customerDao.insertCustomer(row)
            assignCustomerRemoteIdInCurrentTx(id)
            id
        }
    }

    suspend fun updateCustomerPhotoPath(customerId: Long, photoPath: String?) {
        val now = currentTimeMillis()
        customerDao.updateCustomerPhotoPath(customerId, photoPath, now)
        // Keep remote sync payload compatible: customer remote model has no photoPath.
        // We still bump updatedAt so local sort/merges behave consistently.
    }

    suspend fun getCustomer(id: Long): CustomerEntity? = customerDao.getCustomerById(id)

    suspend fun getAllCustomers(): List<CustomerEntity> = customerDao.getAllCustomersList()

    suspend fun deleteCustomerCascade(customerId: Long) {
        val customer = customerDao.getCustomerById(customerId) ?: return
        val loans = loanDao.getLoansForCustomerList(customerId)
        val customerDocId = firestoreCustomerDocId(customer.name, customer.mobile, customer.createdDate)
        for (loan in loans) {
            val emis = emiDao.getEmisForLoan(loan.id).first()
            for (emi in emis) {
                emi.remoteId?.let { eid ->
                    loan.remoteId?.let { lid -> db.enqueueEmiDelete(eid, customerDocId, lid) }
                }
            }
            loan.remoteId?.let { db.enqueueLoanDelete(it, customerDocId) }
        }
        customer.remoteId?.let { db.enqueueCustomerDelete(it, customerDocId) }
        for (loan in loans) {
            emiDao.deleteEmisForLoan(loan.id)
        }
        loanDao.deleteLoansForCustomer(customerId)
        customerDao.deleteCustomerById(customerId)
    }
}
