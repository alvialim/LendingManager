package com.haftabook.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * WHAT: Database table for loans
 * WHERE: Data Layer (Clean Architecture)
 * WHY: Stores loan information
 */
@Entity(tableName = "loans")
data class LoanEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "remote_id")
    val remoteId: String? = null,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = 0,
    val customerId: Long,
    val loanNumber: Int,
    val loanAmount: Long,
    val emiAmount: Long,           // Added: Amount per EMI
    val loanStartDate: Long,
    val emiStartDate: Long,
    val totalEmis: Int,
    val lastEmiDate: Long,
    val remainingAmount: Long
)
