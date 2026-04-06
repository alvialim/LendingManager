package com.haftabook.app.utils

import com.haftabook.app.domain.model.Customer

expect object CommunicationHelper {
    fun sendLoanAddedMessages(customerName: String, amount: Long)
    fun sendEmiAddedMessages(customerName: String, amount: Long, emiNumber: Int)
    /** Opens the system share/compose flow with full customer summary (Android: chooser; desktop: clipboard + mailto). */
    fun sendCustomerMessage(customer: Customer)
    /** Opens SMS compose to admin with a pre-filled message (Android: SMS app; desktop: sms/mailto URL or clipboard). */
    fun openSmsToAdminWithCustomer(customer: Customer)

    /** Share sheet with EMI line details (Android: ACTION_SEND; desktop: clipboard + mailto). */
    fun shareEmiPaymentDetails(
        emiNumber: Int,
        loanNumber: Int,
        customerName: String,
        customerMobile: String,
        amountFormatted: String,
        dateFormatted: String,
    )

    /** SMS compose to EMI admin with EMI details (Android: SMS app; desktop: sms URL or clipboard). */
    fun smsEmiPaymentToAdmin(
        emiNumber: Int,
        loanNumber: Int,
        customerName: String,
        customerMobile: String,
        amountFormatted: String,
        dateFormatted: String,
    )
}
