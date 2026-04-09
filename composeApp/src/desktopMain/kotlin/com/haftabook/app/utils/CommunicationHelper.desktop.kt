package com.haftabook.app.utils

import com.haftabook.app.domain.model.Customer
import java.awt.Desktop
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.net.URI
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/**
 * Desktop: no Android intents — use default mail handler, SMS URL handlers where the OS supports them,
 * and clipboard so the user can paste into WhatsApp Web, email, etc.
 */
actual object CommunicationHelper {

    private fun encodeQuery(s: String): String =
        URLEncoder.encode(s, StandardCharsets.UTF_8).replace("+", "%20")

    private fun copyToClipboard(text: String) {
        runCatching {
            Toolkit.getDefaultToolkit().systemClipboard.setContents(StringSelection(text), null)
        }
    }

    private fun desktopOrNull(): Desktop? =
        if (Desktop.isDesktopSupported()) Desktop.getDesktop() else null

    private fun browse(uri: URI): Boolean {
        val d = desktopOrNull() ?: return false
        if (!d.isSupported(Desktop.Action.BROWSE)) return false
        return runCatching { d.browse(uri) }.isSuccess
    }

    private fun openMailto(subject: String, body: String): Boolean {
        val d = desktopOrNull() ?: return false
        val mailto =
            "mailto:?subject=${encodeQuery(subject)}&body=${encodeQuery(body)}"
        val uri = runCatching { URI(mailto) }.getOrNull() ?: return false
        return runCatching {
            when {
                d.isSupported(Desktop.Action.MAIL) -> {
                    d.mail(uri)
                    true
                }
                d.isSupported(Desktop.Action.BROWSE) -> {
                    d.browse(uri)
                    true
                }
                else -> false
            }
        }.getOrDefault(false)
    }

    private fun openSmsCompose(phoneE164: String, message: String): Boolean {
        val body = encodeQuery(message)
        val digitsOnly = phoneE164.filter { it.isDigit() }
        val candidates = buildList {
            add("sms:$phoneE164?body=$body")
            add("sms:+${digitsOnly}?body=$body")
            if (digitsOnly.isNotEmpty()) add("sms:$digitsOnly?body=$body")
        }
        for (spec in candidates) {
            val uri = runCatching { URI(spec) }.getOrNull() ?: continue
            if (browse(uri)) return true
        }
        val msUri = runCatching {
            URI("ms-sms:?phone=$digitsOnly&body=$body")
        }.getOrNull()
        if (msUri != null && browse(msUri)) return true
        return false
    }

    actual fun sendCustomerMessage(customer: Customer) {
        val message = CustomerCommunicationText.buildCustomerShareMessage(customer)
        copyToClipboard(message)
        openMailto("Haftabook — ${customer.name}", message)
    }

    actual fun openSmsToAdminWithCustomer(customer: Customer) {
        val message = CustomerCommunicationText.buildCustomerSummaryMessage(customer)
        val admin = CustomerCommunicationText.ADMIN_NUMBER
        if (!openSmsCompose(admin, message)) {
            copyToClipboard(
                buildString {
                    appendLine("SMS to $admin")
                    appendLine()
                    append(message)
                }
            )
        }
    }

    actual fun sendLoanAddedMessages(customerName: String, amount: Long) {
        println("[Haftabook] Loan added: $customerName — ₹$amount (SMS not available on desktop)")
    }

    actual fun sendEmiAddedMessages(customerName: String, amount: Long, emiNumber: Int) {
        println("[Haftabook] EMI paid: $customerName #$emiNumber — ₹$amount (SMS not available on desktop)")
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
        copyToClipboard(message)
        openMailto("Haftabook EMI #$emiNumber — $customerName", message)
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
        val admin = CustomerCommunicationText.EMI_ADMIN_NUMBER
        if (!openSmsCompose(admin, message)) {
            copyToClipboard(
                buildString {
                    appendLine("SMS to $admin")
                    appendLine()
                    append(message)
                }
            )
        }
    }

    actual fun sendForgotPinOtp(otp: String) {
        val message = CustomerCommunicationText.buildPinResetOtpMessage(otp)
        val admin = CustomerCommunicationText.PIN_RESET_OTP_NUMBER
        if (!openSmsCompose(admin, message)) {
            copyToClipboard(
                buildString {
                    appendLine("SMS to $admin")
                    appendLine()
                    append(message)
                }
            )
        }
    }
}
