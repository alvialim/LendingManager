package com.haftabook.app.domain.model

/**
 * WHAT: Business model for customer
 * WHERE: Domain Layer (Clean Architecture)
 * WHY: UI uses this, not database entity
 * 
 * SOLID: Single Responsibility
 * - Represents customer for UI
 * - Different from database entity
 * - Can have calculated fields
 */
data class Customer(
    val id: Long,
    val name: String,
    val mobile: String,
    val loanType: String,
    val createdDate: Long,
    val photoPath: String? = null,

    // Calculated fields (from loans)
    val totalGiven: Long = 0,      // Sum of all loan amounts
    val totalPaid: Long = 0,       // Sum of all EMI payments
    val totalDue: Long = 0,        // Total remaining
    val totalLoans: Int = 0        // Number of loans
)
// This is what home screen shows

// Simple: This is what UI needs
// Different from database entity because:
// - Database entity = raw data
// - Business model = data + calculations