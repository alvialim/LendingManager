package com.haftabook.app.utils


import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Simple date helper functions
 */
object DateHelper {

    private val formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy")
    private val zone = ZoneId.systemDefault()

    // Convert timestamp to readable date
    // Example: 1705564800000 -> "18-Jan-2024"
    fun formatDate(timestamp: Long): String {
        val date = Instant.ofEpochMilli(timestamp)
            .atZone(zone)
            .toLocalDate()
        return date.format(formatter)
    }

    // Get current time as timestamp
    fun now(): Long {
        return System.currentTimeMillis()
    }

    // Add days to a date
    fun addDays(timestamp: Long, days: Int): Long {
        val date = Instant.ofEpochMilli(timestamp)
            .atZone(zone)
            .toLocalDate()
        val newDate = date.plusDays(days.toLong())
        return newDate.atStartOfDay(zone).toInstant().toEpochMilli()
    }

    // Add months to a date
    fun addMonths(timestamp: Long, months: Int): Long {
        val date = Instant.ofEpochMilli(timestamp)
            .atZone(zone)
            .toLocalDate()
        val newDate = date.plusMonths(months.toLong())
        return newDate.atStartOfDay(zone).toInstant().toEpochMilli()
    }
}