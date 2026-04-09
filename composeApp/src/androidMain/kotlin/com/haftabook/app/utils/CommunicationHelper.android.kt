package com.haftabook.app.utils

import android.content.Intent
import android.net.Uri
import android.util.Log
import com.haftabook.app.AndroidAppContext
import com.haftabook.app.domain.model.Customer

actual object CommunicationHelper {

    private const val TAG = "Haftabook"

    actual fun sendCustomerMessage(customer: Customer) {
        val message = CustomerCommunicationText.buildCustomerShareMessage(customer)
        val context = AndroidAppContext.applicationContext ?: return
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, message)
        }
        val chooser = Intent.createChooser(intent, "Share").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    actual fun openSmsToAdminWithCustomer(customer: Customer) {
        val message = CustomerCommunicationText.buildCustomerSummaryMessage(customer)
        val context = AndroidAppContext.applicationContext ?: return
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("smsto:${Uri.encode(CustomerCommunicationText.ADMIN_NUMBER)}")
            putExtra("sms_body", message)
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    actual fun sendLoanAddedMessages(customerName: String, amount: Long) {
        Log.d(TAG, "New Loan Added! Customer: $customerName Amount: ₹$amount (no SMS permission)")
    }

    actual fun sendEmiAddedMessages(customerName: String, amount: Long, emiNumber: Int) {
        Log.d(TAG, "EMI Paid! Customer: $customerName EMI No: $emiNumber Amount: ₹$amount (no SMS permission)")
    }

    actual fun shareEmiPaymentDetails(
        emiNumber: Int,
        loanNumber: Int,
        customerName: String,
        customerMobile: String,
        amountFormatted: String,
        dateFormatted: String,
    ) {
        val message = CustomerCommunicationText.buildEmiMessage(
            emiNumber, loanNumber, customerName, customerMobile, amountFormatted, dateFormatted
        )
        val context = AndroidAppContext.applicationContext ?: return
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, message)
        }
        val chooser = Intent.createChooser(intent, "Share").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    actual fun smsEmiPaymentToAdmin(
        emiNumber: Int,
        loanNumber: Int,
        customerName: String,
        customerMobile: String,
        amountFormatted: String,
        dateFormatted: String,
    ) {
        val message = CustomerCommunicationText.buildEmiMessage(
            emiNumber, loanNumber, customerName, customerMobile, amountFormatted, dateFormatted
        )
        val context = AndroidAppContext.applicationContext ?: return
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("smsto:${Uri.encode(CustomerCommunicationText.EMI_ADMIN_NUMBER)}")
            putExtra("sms_body", message)
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    actual fun sendForgotPinOtp(otp: String) {
        sendForgotPinOtpInBackgroundOrRequest(otp)
    }
}
