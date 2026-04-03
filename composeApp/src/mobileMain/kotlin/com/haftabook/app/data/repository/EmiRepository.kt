package com.haftabook.app.data.repository

import com.haftabook.app.data.AppDatabase
import com.haftabook.app.data.local.entity.EmiEntity
import com.haftabook.app.data.sync.enqueueLoanUpsertAfterChange
import com.haftabook.app.data.sync.ensureEmiRemoteId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class EmiRepository(
    private val db: AppDatabase
) {
    private val emiDao get() = db.emiDao()

    fun getEmisForLoan(loanId: Long): Flow<List<EmiEntity>> =
        emiDao.getEmisForLoan(loanId)

    suspend fun addEmi(emi: EmiEntity): Long {
        val now = System.currentTimeMillis()
        val row = emi.copy(updatedAt = now)
        val id = emiDao.insertEmi(row)
        db.ensureEmiRemoteId(id)
        db.enqueueLoanUpsertAfterChange(emi.loanId)
        return id
    }

    suspend fun getNextEmiNumber(loanId: Long): Int =
        emiDao.getNextEmiNumber(loanId)

    suspend fun getTotalPaid(loanId: Long): Long =
        emiDao.getTotalPaidForLoan(loanId).first() ?: 0L

    suspend fun deleteEmisByLoan(loanId: Long) {
        emiDao.deleteEmisForLoan(loanId)
    }

    suspend fun getAllEmis(): List<EmiEntity> = emiDao.getAllEmisList()
}
