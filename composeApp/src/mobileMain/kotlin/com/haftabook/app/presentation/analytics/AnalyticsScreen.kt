package com.haftabook.app.presentation.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.haftabook.app.domain.usecase.AnalyticsBucket
import com.haftabook.app.domain.usecase.AnalyticsGranularity
import com.haftabook.app.presentation.components.ResponsiveCentered
import com.haftabook.app.ui.PaidAmountGreen
import com.haftabook.app.utils.NumberHelper

/** Given — blue, Paid — green, Due — red, Customers — purple. */
private val GivenBarBlue = Color(0xFF1976D2)
private val DueBarRed = Color(0xFFD32F2F)
private val CustomersBarPurple = Color(0xFF7E57C2)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    viewModel: AnalyticsViewModel,
    onBack: () -> Unit,
) {
    val granularity = viewModel.granularity
    val buckets = viewModel.buckets
    val isLoading = viewModel.isLoading
    val granularityScroll = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Analytics") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        ResponsiveCentered(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            expandedMaxWidth = 980.dp,
            contentAlignment = Alignment.TopCenter
        ) { inner ->
            Column(
                modifier = inner
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(granularityScroll)
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AnalyticsGranularity.entries.forEach { g ->
                    FilterChip(
                        selected = granularity == g,
                        onClick = { viewModel.onGranularityChange(g) },
                        label = {
                            Text(
                                when (g) {
                                    AnalyticsGranularity.Yearly -> "Yearly"
                                    AnalyticsGranularity.Monthly -> "Monthly"
                                    AnalyticsGranularity.Weekly -> "Weekly"
                                    AnalyticsGranularity.Daily -> "Daily"
                                }
                            )
                        }
                    )
                }
            }

            LegendRow(
                givenColor = GivenBarBlue,
                paidColor = PaidAmountGreen,
                dueColor = DueBarRed,
                customersColor = CustomersBarPurple
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (buckets.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No data to show yet.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                val maxVal = remember(buckets) {
                    buckets.maxOf { maxOf(it.given, it.paid, it.due) }.coerceAtLeast(1L)
                }
                val maxCount = remember(buckets) {
                    buckets.maxOf { it.customerCount }.coerceAtLeast(1)
                }
                val trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                AnalyticsHorizontalChart(
                    granularity = granularity,
                    buckets = buckets,
                    maxMoneyValue = maxVal,
                    maxCustomerCount = maxCount,
                    givenColor = GivenBarBlue,
                    paidColor = PaidAmountGreen,
                    dueColor = DueBarRed,
                    customersColor = CustomersBarPurple,
                    trackColor = trackColor,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
            }
            }
        }
    }
}

@Composable
private fun LegendRow(
    givenColor: Color,
    paidColor: Color,
    dueColor: Color,
    customersColor: Color,
) {
    val legendScroll = rememberScrollState()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(legendScroll),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LegendItem("Given", givenColor)
        LegendItem("Paid", paidColor)
        LegendItem("Due", dueColor)
        LegendItem("Customers", customersColor)
    }
}

@Composable
private fun LegendItem(label: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(color)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun AnalyticsHorizontalChart(
    granularity: AnalyticsGranularity,
    buckets: List<AnalyticsBucket>,
    maxMoneyValue: Long,
    maxCustomerCount: Int,
    givenColor: Color,
    paidColor: Color,
    dueColor: Color,
    customersColor: Color,
    trackColor: Color,
    modifier: Modifier = Modifier,
) {
    val scroll = rememberScrollState()
    val maxMoney = maxMoneyValue.coerceAtLeast(1L)
    val maxCust = maxCustomerCount.coerceAtLeast(1)
    val labelColumnWidth = when (granularity) {
        AnalyticsGranularity.Monthly -> 120.dp
        AnalyticsGranularity.Daily -> 88.dp
        AnalyticsGranularity.Weekly -> 96.dp
        AnalyticsGranularity.Yearly -> 56.dp
    }
    Column(
        modifier = modifier.verticalScroll(scroll)
    ) {
        buckets.forEach { bucket ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = bucket.label,
                    modifier = Modifier.widthIn(min = labelColumnWidth, max = 140.dp),
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    HorizontalBarRow(
                        value = bucket.given,
                        max = maxMoney,
                        color = givenColor,
                        trackColor = trackColor
                    )
                    HorizontalBarRow(
                        value = bucket.paid,
                        max = maxMoney,
                        color = paidColor,
                        trackColor = trackColor
                    )
                    HorizontalBarRow(
                        value = bucket.due,
                        max = maxMoney,
                        color = dueColor,
                        trackColor = trackColor
                    )
                    CustomerCountBarRow(
                        count = bucket.customerCount,
                        max = maxCust,
                        color = customersColor,
                        trackColor = trackColor
                    )
                }
            }
        }
    }
}

@Composable
private fun HorizontalBarRow(
    value: Long,
    max: Long,
    color: Color,
    trackColor: Color,
) {
    val fraction = if (max > 0) (value.toFloat() / max.toFloat()).coerceIn(0f, 1f) else 0f
    val amountText = "₹${NumberHelper.formatMoney(value)}"
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(4.dp))
                    .background(trackColor)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(4.dp))
                    .background(color)
            )
        }
        Text(
            text = amountText,
            modifier = Modifier.widthIn(min = 96.dp),
            style = MaterialTheme.typography.labelMedium,
            color = color,
            maxLines = 1
        )
    }
}

@Composable
private fun CustomerCountBarRow(
    count: Int,
    max: Int,
    color: Color,
    trackColor: Color,
) {
    val fraction = if (max > 0) (count.toFloat() / max.toFloat()).coerceIn(0f, 1f) else 0f
    val countText = if (count == 1) "1 customer" else "$count customers"
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(4.dp))
                    .background(trackColor)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(4.dp))
                    .background(color)
            )
        }
        Text(
            text = countText,
            modifier = Modifier.widthIn(min = 96.dp),
            style = MaterialTheme.typography.labelMedium,
            color = color,
            maxLines = 1
        )
    }
}
