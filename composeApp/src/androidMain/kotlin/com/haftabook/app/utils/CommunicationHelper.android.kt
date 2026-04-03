package com.haftabook.app.utils

import android.os.Build
import android.telephony.SmsManager
import com.haftabook.app.AndroidAppContext

actual object CommunicationHelper {

    private const val ADMIN_NUMBER = "+919974373447"

    actual fun sendLoanAddedMessages(customerName: String, amount: Long) {
        val message = "New Loan Added!\nCustomer: $customerName\nAmount: ₹$amount"
        sendSMS(message)
    }

    actual fun sendEmiAddedMessages(customerName: String, amount: Long, emiNumber: Int) {
        val message = "EMI Paid!\nCustomer: $customerName\nEMI No: $emiNumber\nAmount: ₹$amount"
        sendSMS(message)
    }

    private fun sendSMS(message: String) {
        val context = AndroidAppContext.applicationContext ?: return
        try {
            val smsManager: SmsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                context.getSystemService(SmsManager::class.java)!!
            } else {
                @Suppress("DEPRECATION")
                SmsManager.getDefault()
            }
            smsManager.sendTextMessage(
                ADMIN_NUMBER,
                null,
                message,
                null,
                null
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
