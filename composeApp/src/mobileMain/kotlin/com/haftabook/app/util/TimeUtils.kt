package com.haftabook.app.util

/**
 * Get current time in milliseconds.
 * Using expect/actual avoids NoClassDefFoundError issues with external libraries in some runtimes.
 */
expect fun currentTimeMillis(): Long
