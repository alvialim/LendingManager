package com.haftabook.app.domain.model

/**
 * WHAT: Loan domain model
 * WHERE: Domain Layer
 */
data class Loan(
    val id: Long,
    val customerId: Long,
    val loanNumber: Int,
    val loanAmount: Long,
    val emiAmount: Long,           // Added EMI amount
    val loanStartDate: Long,
    val emiStartDate: Long,
    val totalEmis: Int,
    val lastEmiDate: Long,
    val remainingAmount: Long
)

data class Emi(
    val id: Long,
    val loanId: Long,
    val emiNumber: Int,
    val emiAmount: Long,
    val emiDate: Long
)
