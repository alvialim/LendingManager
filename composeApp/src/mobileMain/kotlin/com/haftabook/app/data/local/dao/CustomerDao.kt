package com.haftabook.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.haftabook.app.data.local.entity.CustomerEntity
import com.haftabook.app.data.local.model.CustomerWithTotals
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: CustomerEntity): Long

    @Query("SELECT * FROM customers ORDER BY createdDate DESC")
    fun getAllCustomers(): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers ORDER BY createdDate DESC")
    suspend fun getAllCustomersList(): List<CustomerEntity>

    @Query(
        """
        SELECT
            c.*,
            (SELECT COALESCE(SUM(loanAmount), 0) FROM loans WHERE customerId = c.id) AS totalGiven,
            (SELECT COALESCE(SUM(remainingAmount), 0) FROM loans WHERE customerId = c.id) AS totalDue,
            (SELECT COUNT(*) FROM loans WHERE customerId = c.id) AS totalLoans
        FROM customers c
        ORDER BY c.createdDate DESC
        """
    )
    fun getCustomersWithTotals(): Flow<List<CustomerWithTotals>>

    /** Same rows as [getCustomersWithTotals] for one-shot reads after writes (Flow can lag briefly). */
    @Query(
        """
        SELECT
            c.*,
            (SELECT COALESCE(SUM(loanAmount), 0) FROM loans WHERE customerId = c.id) AS totalGiven,
            (SELECT COALESCE(SUM(remainingAmount), 0) FROM loans WHERE customerId = c.id) AS totalDue,
            (SELECT COUNT(*) FROM loans WHERE customerId = c.id) AS totalLoans
        FROM customers c
        ORDER BY c.createdDate DESC
        """
    )
    suspend fun getCustomersWithTotalsSnapshot(): List<CustomerWithTotals>

    @Query("SELECT * FROM customers WHERE id = :id")
    suspend fun getCustomerById(id: Long): CustomerEntity?

    @Query("SELECT * FROM customers WHERE remote_id = :remoteId LIMIT 1")
    suspend fun getCustomerByRemoteId(remoteId: String): CustomerEntity?

    @Delete
    suspend fun deleteCustomer(customer: CustomerEntity)

    @Query("DELETE FROM customers")
    suspend fun deleteAllCustomers()

    @Query("DELETE FROM customers WHERE id = :customerId")
    suspend fun deleteCustomerById(customerId: Long)
}
