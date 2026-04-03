package com.haftabook.app.utils

actual object CommunicationHelper {
    actual fun sendLoanAddedMessages(customerName: String, amount: Long) {
        println("[Haftabook] Loan added: $customerName — ₹$amount (SMS not available on desktop)")
    }

    actual fun sendEmiAddedMessages(customerName: String, amount: Long, emiNumber: Int) {
        println("[Haftabook] EMI paid: $customerName #$emiNumber — ₹$amount (SMS not available on desktop)")
    }
}
