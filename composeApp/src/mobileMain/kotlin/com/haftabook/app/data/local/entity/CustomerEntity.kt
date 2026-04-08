package com.haftabook.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * WHAT: Database table for customers
 * WHERE: Data Layer (Clean Architecture)
 * WHY: Stores customer information in database
 *
 * SOLID: Single Responsibility
 * - This class ONLY represents database table
 * - It does NOT handle UI, business logic, or calculations
 */
@Entity(tableName = "customers")
data class CustomerEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "remote_id")
    val remoteId: String? = null,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = 0,
    val name: String,
    val mobile: String,
    val loanType: String,      // "DAILY" or "MONTHLY"
    val createdDate: Long,
    /** Absolute file path to a locally stored JPEG profile photo (optional). */
    val photoPath: String? = null
)

// Simple: Just data, nothing else
