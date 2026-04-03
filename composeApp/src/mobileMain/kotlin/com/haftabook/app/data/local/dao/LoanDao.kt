package com.haftabook.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.haftabook.app.data.local.entity.LoanEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LoanDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLoan(loan: LoanEntity): Long

    @Query("SELECT * FROM loans WHERE customerId = :customerId ORDER BY loanStartDate DESC")
    fun getLoansForCustomer(customerId: Long): Flow<List<LoanEntity>>

    @Query("SELECT * FROM loans WHERE id = :loanId")
    suspend fun getLoanById(loanId: Long): LoanEntity?

    @Query("UPDATE loans SET remainingAmount = :remaining WHERE id = :loanId")
    suspend fun updateRemainingAmount(loanId: Long, remaining: Long)

    @Query("DELETE FROM loans WHERE customerId = :customerId")
    suspend fun deleteLoansForCustomer(customerId: Long)

    @Query("SELECT SUM(loanAmount) FROM loans WHERE customerId = :customerId")
    fun getTotalLoanForCustomer(customerId: Long): Flow<Long?>

    @Query("SELECT SUM(remainingAmount) FROM loans WHERE customerId = :customerId")
    fun getTotalRemainingForCustomer(customerId: Long): Flow<Long?>

    @Query("SELECT COALESCE(MAX(loanNumber), 0) + 1 FROM loans WHERE customerId = :customerId")
    suspend fun getNextLoanNumber(customerId: Long): Int

    @Query("DELETE FROM loans WHERE id = :loanId")
    suspend fun deleteLoanById(loanId: Long)

    @Query("SELECT * FROM loans WHERE customerId = :customerId")
    suspend fun getLoansForCustomerList(customerId: Long): List<LoanEntity>

    @Query("SELECT * FROM loans WHERE remote_id = :remoteId LIMIT 1")
    suspend fun getLoanByRemoteId(remoteId: String): LoanEntity?

    @Query("SELECT * FROM loans")
    suspend fun getAllLoansList(): List<LoanEntity>

    @Query("DELETE FROM loans")
    suspend fun deleteAllLoans()
}
