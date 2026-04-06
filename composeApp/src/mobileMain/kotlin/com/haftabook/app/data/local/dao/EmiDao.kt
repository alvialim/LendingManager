package com.haftabook.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.haftabook.app.data.local.entity.EmiEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EmiDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmi(emi: EmiEntity): Long

    @Query("SELECT * FROM emis WHERE id = :id")
    suspend fun getEmiById(id: Long): EmiEntity?

    @Query("SELECT * FROM emis WHERE loanId = :loanId ORDER BY emiNumber ASC")
    fun getEmisForLoan(loanId: Long): Flow<List<EmiEntity>>

    @Query("SELECT * FROM emis WHERE loanId = :loanId AND emiNumber = :emiNumber LIMIT 1")
    suspend fun getEmiByLoanAndNumber(loanId: Long, emiNumber: Int): EmiEntity?

    @Query("SELECT COUNT(*) + 1 FROM emis WHERE loanId = :loanId")
    suspend fun getNextEmiNumber(loanId: Long): Int

    @Query("SELECT SUM(emiAmount) FROM emis WHERE loanId = :loanId")
    fun getTotalPaidForLoan(loanId: Long): Flow<Long?>

    @Query("DELETE FROM emis WHERE loanId = :loanId")
    suspend fun deleteEmisForLoan(loanId: Long)

    @Query("SELECT * FROM emis WHERE remote_id = :remoteId LIMIT 1")
    suspend fun getEmiByRemoteId(remoteId: String): EmiEntity?

    @Query("SELECT * FROM emis")
    suspend fun getAllEmisList(): List<EmiEntity>

    @Query("DELETE FROM emis")
    suspend fun deleteAllEmis()
}
