package com.haftabook.app.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CustomerRemote(
    val id: String,
    val name: String,
    val mobile: String,
    @SerialName("loan_type") val loanType: String,
    @SerialName("created_date") val createdDate: Long,
    @SerialName("updated_at") val updatedAt: Long
)

@Serializable
data class LoanRemote(
    val id: String,
    @SerialName("customer_id") val customerId: String,
    @SerialName("loan_number") val loanNumber: Int,
    @SerialName("loan_amount") val loanAmount: Long,
    @SerialName("emi_amount") val emiAmount: Long,
    @SerialName("loan_start_date") val loanStartDate: Long,
    @SerialName("emi_start_date") val emiStartDate: Long,
    @SerialName("total_emis") val totalEmis: Int,
    @SerialName("last_emi_date") val lastEmiDate: Long,
    @SerialName("remaining_amount") val remainingAmount: Long,
    @SerialName("updated_at") val updatedAt: Long
)

@Serializable
data class EmiRemote(
    val id: String,
    @SerialName("loan_id") val loanId: String,
    @SerialName("emi_number") val emiNumber: Int,
    @SerialName("emi_amount") val emiAmount: Long,
    @SerialName("emi_date") val emiDate: Long,
    @SerialName("created_at") val createdAt: Long,
    @SerialName("updated_at") val updatedAt: Long
)
