package com.haftabook.app.utils



import java.text.NumberFormat
import java.util.Locale

/**
 * Simple number formatting
 */
object NumberHelper {

    private val formatter = NumberFormat.getInstance(Locale("en", "IN"))

    // Format number with commas
    // Example: 175000 -> "1,75,000"
    fun formatMoney(amount: Long): String {
        return formatter.format(amount)
    }
}