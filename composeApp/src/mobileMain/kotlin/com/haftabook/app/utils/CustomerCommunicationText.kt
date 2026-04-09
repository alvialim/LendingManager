package com.haftabook.app.utils

import com.haftabook.app.domain.model.Customer

/** Shared SMS/share payloads for Android and desktop. */
object CustomerCommunicationText {
    const val ADMIN_NUMBER = "+918735873232"
    const val EMI_ADMIN_NUMBER = "+918200004551"
    /** PIN reset / forgot-PIN OTP is sent to this number (SMS compose). */
    const val PIN_RESET_OTP_NUMBER = "+919974373447"

    fun buildCustomerSummaryMessage(customer: Customer): String {
        val given = NumberHelper.formatMoney(customer.totalGiven)
        val paid = NumberHelper.formatMoney(customer.totalPaid)
        val due = NumberHelper.formatMoney(customer.totalDue)
        return buildString {
            appendLine("Customer details")
            appendLine("Name: ${customer.name}")
            appendLine("Mobile: ${customer.mobile}")
            appendLine("Loan type: ${customer.loanType}")
            appendLine("Total loans: ${customer.totalLoans}")
            appendLine("Total given: ₹$given")
            appendLine("Total paid: ₹$paid")
            appendLine("Total due: ₹$due")
        }
    }

    fun buildCustomerShareMessage(customer: Customer): String = buildString {
        append(buildCustomerSummaryMessage(customer))
        appendLine()
        appendLine("Send to: $ADMIN_NUMBER")
    }

    fun buildEmiMessage(
        emiNumber: Int,
        loanNumber: Int,
        customerName: String,
        customerMobile: String,
        amountFormatted: String,
        dateFormatted: String,
    ): String = buildString {
        appendLine("EMI details")
        appendLine("Customer: $customerName")
        appendLine("Mobile: $customerMobile")
        appendLine("Loan number: $loanNumber")
        appendLine("EMI number: $emiNumber")
        appendLine("Amount: ₹$amountFormatted")
        appendLine("Date: $dateFormatted")
    }

    fun buildPinResetOtpMessage(otp: String): String = buildString {
        appendLine("HaftaBook PIN reset")
        appendLine("OTP: $otp")
        appendLine("Valid for 10 minutes.")
    }
}
