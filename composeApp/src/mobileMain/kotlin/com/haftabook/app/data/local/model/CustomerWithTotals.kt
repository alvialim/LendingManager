package com.haftabook.app.data.local.model

import androidx.room.Embedded
import com.haftabook.app.data.local.entity.CustomerEntity

/**
 * Data class to hold customer info and their loan totals
 * This is used for mapping database results to business models
 */
data class CustomerWithTotals(
    @Embedded
    val customer: CustomerEntity,
    val totalGiven: Long?,
    val totalDue: Long?,
    val totalLoans: Int
)
