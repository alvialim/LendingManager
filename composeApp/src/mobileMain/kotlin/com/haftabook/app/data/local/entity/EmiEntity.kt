package com.haftabook.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * WHAT: Database table for EMI payments
 * WHERE: Data Layer (Clean Architecture)
 * WHY: Stores each EMI payment
 *
 * SOLID: Single Responsibility
 * - Only represents EMI payment table
 */
@Entity(tableName = "emis")
data class EmiEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "remote_id")
    val remoteId: String? = null,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = 0,
    val loanId: Long,              // Which loan
    val emiNumber: Int,            // EMI 1, 2, 3...
    val emiAmount: Long,           // Amount paid (in rupees)
    val emiDate: Long,             // Payment date
    val createdAt: Long            // When recorded
)

// Simple: Just EMI data