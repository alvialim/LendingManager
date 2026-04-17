package com.haftabook.app.presentation.customer

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Sms
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.haftabook.app.domain.model.Emi
import com.haftabook.app.domain.model.Loan
import com.haftabook.app.presentation.theme.accentBorderForLoan
import com.haftabook.app.ui.PaidAmountGreen
import com.haftabook.app.utils.CommunicationHelper
import com.haftabook.app.utils.DateHelper
import com.haftabook.app.utils.NumberHelper

/**
 * Collapsed: Loan #, amount → due, Delete, expand chevron.
 * Expanded: loan details + EMI 1..N slots (Share, SMS, PENDING/PAID).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoanCard(
    loan: Loan,
    customerName: String,
    customerMobile: String,
    loanType: String,
    isExpanded: Boolean,
    emis: List<Emi>,
    onLoanClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onMarkEmiSlotPaid: (emiNumber: Int) -> Unit,
) {
    val loanBorder = remember(loan.id) { accentBorderForLoan(loan.id) }
    val emiByNumber = remember(emis) { emis.associateBy { it.emiNumber } }

    Card(
        onClick = onLoanClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, loanBorder)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Loan ${loan.loanNumber}",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = if (loanType == "MONTHLY") {
                                "₹${NumberHelper.formatMoney(loan.loanAmount)}"
                            } else {
                                "₹${NumberHelper.formatMoney(loan.loanAmount)} → ₹${NumberHelper.formatMoney(loan.remainingAmount)} due"
                            },
                            style = MaterialTheme.typography.titleSmall,
                            color = if (loanType == "MONTHLY") MaterialTheme.colorScheme.onSurfaceVariant else Color(0xFFF44336),
                            maxLines = 2
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                        modifier = Modifier
                            .rotate(if (isExpanded) 180f else 0f)
                            .padding(start = 4.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                val deleteRed = Color(0xFFDC2626)
                val deleteShape = RoundedCornerShape(4.dp)
                Text(
                    text = "Delete",
                    style = MaterialTheme.typography.labelMedium,
                    color = deleteRed,
                    modifier = Modifier
                        .border(1.dp, deleteRed, deleteShape)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .clickable(onClick = onDeleteClick)
                )
            }

            if (isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 16.dp)
                ) {
                    LoanDetailRow("Loan Start Date", DateHelper.formatDate(loan.loanStartDate))
                    LoanDetailRow("First EMI Date", DateHelper.formatDate(loan.emiStartDate))
                    LoanDetailRow("Total EMIs", loan.totalEmis.toString())
                    LoanDetailRow("Last EMI Date", DateHelper.formatDate(loan.lastEmiDate))
                    LoanDetailRow("EMI Amount", "₹${NumberHelper.formatMoney(loan.emiAmount)}")

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(text = "EMI Payments", style = MaterialTheme.typography.labelLarge)
                    Spacer(modifier = Modifier.height(8.dp))

                    for (slot in 1..loan.totalEmis) {
                        EmiSlotRow(
                            slotNumber = slot,
                            loan = loan,
                            loanType = loanType,
                            emi = emiByNumber[slot],
                            customerName = customerName,
                            customerMobile = customerMobile,
                            onMarkPaid = { onMarkEmiSlotPaid(slot) },
                        )
                        if (slot < loan.totalEmis) {
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmiSlotRow(
    slotNumber: Int,
    loan: Loan,
    loanType: String,
    emi: Emi?,
    customerName: String,
    customerMobile: String,
    onMarkPaid: () -> Unit,
) {
    val isPaid = emi != null
    val scheduledDate = DateHelper.scheduledEmiDate(loan.emiStartDate, slotNumber, loanType)
    val amount = emi?.emiAmount ?: loan.emiAmount
    // Always show this slot's scheduled due date (same before/after marking paid).
    val dateForDisplay = scheduledDate
    val amountStr = NumberHelper.formatMoney(amount)
    val dateStr = DateHelper.formatDate(dateForDisplay)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      /*  Checkbox(
            checked = isPaid,
            onCheckedChange = {},
            enabled = false,
            modifier = Modifier.size(24.dp),
            colors = CheckboxDefaults.colors(
                checkedColor = PaidAmountGreen,
                uncheckedColor = MaterialTheme.colorScheme.outline,
                disabledCheckedColor = PaidAmountGreen,
                disabledUncheckedColor = MaterialTheme.colorScheme.outline,
            )
        )*/
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "EMI $slotNumber",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "₹$amountStr · $dateStr",
                style = MaterialTheme.typography.bodySmall,
                color = if (isPaid) PaidAmountGreen else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        IconButton(
            onClick = {
                CommunicationHelper.shareEmiPaymentDetails(
                    emiNumber = slotNumber,
                    loanNumber = loan.loanNumber,
                    customerName = customerName,
                    customerMobile = customerMobile,
                    amountFormatted = amountStr,
                    dateFormatted = dateStr,
                )
            },
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                Icons.Outlined.Share,
                contentDescription = "Share EMI $slotNumber",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
        IconButton(
            onClick = {
                CommunicationHelper.smsEmiPaymentToAdmin(
                    emiNumber = slotNumber,
                    loanNumber = loan.loanNumber,
                    customerName = customerName,
                    customerMobile = customerMobile,
                    amountFormatted = amountStr,
                    dateFormatted = dateStr,
                )
            },
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                Icons.Outlined.Sms,
                contentDescription = "SMS EMI $slotNumber",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
        val payColor = if (isPaid) PaidAmountGreen else Color(0xFFDC2626)
        val payShape = RoundedCornerShape(4.dp)
        TextButton(
            onClick = onMarkPaid,
            enabled = !isPaid,
            modifier = Modifier
                .widthIn(min = 72.dp)
                .border(1.dp, payColor, payShape),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
            colors = ButtonDefaults.textButtonColors(
                contentColor = payColor,
                disabledContentColor = payColor,
            )
        ) {
            Text(
                text = if (isPaid) "PAID" else "PAY",
                style = MaterialTheme.typography.labelMedium,
                color = payColor
            )
        }
    }
}

@Composable
fun LoanDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = "$label:", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        Text(text = value, style = MaterialTheme.typography.bodyMedium)
    }
}
