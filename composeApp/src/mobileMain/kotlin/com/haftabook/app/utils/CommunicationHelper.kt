package com.haftabook.app.utils

expect object CommunicationHelper {
    fun sendLoanAddedMessages(customerName: String, amount: Long)
    fun sendEmiAddedMessages(customerName: String, amount: Long, emiNumber: Int)
}
