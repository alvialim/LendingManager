package com.haftabook.app.presentation.theme

import androidx.compose.ui.graphics.Color

private val LightCardBorderPalette = listOf(
    Color(0xFF7EC8E3),
    Color(0xFF81C784),
    Color(0xFFFFB74D),
    Color(0xFFBA68C8),
    Color(0xFFF06292),
    Color(0xFFAED581),
    Color(0xFF64B5F6),
    Color(0xFF90A4AE),
    Color(0xFF4DD0E1),
    Color(0xFFFFD54F),
)

private val LightButtonBackgroundPalette = listOf(
    Color(0xFFE3F7FD),
    Color(0xFFE8F5E9),
    Color(0xFFFFF8E1),
    Color(0xFFF3E5F5),
    Color(0xFFFCE4EC),
    Color(0xFFF1F8E9),
    Color(0xFFE8EAF6),
    Color(0xFFECEFF1),
    Color(0xFFE0F7FA),
    Color(0xFFFFFDE7),
)

private fun stableBucket(seed: Long, salt: Int, size: Int): Int {
    val mixed = (seed * 31L + salt * 17L) and Long.MAX_VALUE
    return (mixed % size.toLong()).toInt()
}

/** Stable “random” light border for a customer (home card + detail summary). */
fun accentBorderForCustomer(customerId: Long): Color =
    LightCardBorderPalette[stableBucket(customerId, 0, LightCardBorderPalette.size)]

/** Stable light border for each loan card on the detail screen. */
fun accentBorderForLoan(loanId: Long): Color =
    LightCardBorderPalette[stableBucket(loanId, 42, LightCardBorderPalette.size)]

/** Light background for Share / SMS / Delete (slot 0, 1, 2). */
fun lightActionButtonBackground(customerId: Long, actionSlot: Int): Color =
    LightButtonBackgroundPalette[stableBucket(customerId, actionSlot * 31, LightButtonBackgroundPalette.size)]
