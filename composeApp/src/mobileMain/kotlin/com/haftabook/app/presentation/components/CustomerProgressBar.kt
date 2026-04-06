package com.haftabook.app.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.haftabook.app.domain.model.Customer

private val CustomerProgressPaidOlive = Color(0xFF6B8E23)
private val CustomerProgressRemainingLightRed = Color(0xFFFFCDD2)
private val CustomerProgressTrackGrey = Color(0xFFE8E8E8)

/** Same progress bar as the home customer card (paid vs due share of given). */
@Composable
fun CustomerProgressBar(customer: Customer) {
    val given = customer.totalGiven
    val paid = customer.totalPaid
    val due = customer.totalDue
    val (paidF, dueF, hasPaidProgress) = remember(given, paid, due) {
        val paidF = if (given > 0) paid.toFloat() / given.toFloat() else 0f
        val dueF = if (given > 0) due.toFloat() / given.toFloat() else 0f
        val hasPaidProgress = given > 0 && paid > 0
        Triple(paidF, dueF, hasPaidProgress)
    }
    val shape = RoundedCornerShape(4.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(shape)
            .background(
                if (hasPaidProgress) CustomerProgressTrackGrey
                else CustomerProgressRemainingLightRed
            )
    ) {
        if (hasPaidProgress) {
            Row(modifier = Modifier.fillMaxSize()) {
                if (paidF > 0f) {
                    Box(
                        modifier = Modifier
                            .weight(paidF)
                            .fillMaxHeight()
                            .background(CustomerProgressPaidOlive)
                    )
                }
                if (dueF > 0f) {
                    Box(
                        modifier = Modifier
                            .weight(dueF)
                            .fillMaxHeight()
                            .background(CustomerProgressRemainingLightRed)
                    )
                }
            }
        }
    }
}
