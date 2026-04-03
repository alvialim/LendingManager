package com.haftabook.app.presentation.customer

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.haftabook.app.domain.model.Emi
import com.haftabook.app.domain.model.Loan
import com.haftabook.app.presentation.components.DeleteActionButton
import com.haftabook.app.utils.DateHelper
import com.haftabook.app.utils.NumberHelper

/**
 * Collapsed: Loan #, amount → due, delete, expand chevron.
 * Expanded: loan details, EMI list, Add EMI.
 * The whole card toggles expand/collapse; Delete / Add EMI remain separate actions.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoanCard(
    loan: Loan,
    isExpanded: Boolean,
    emis: List<Emi>,
    onLoanClick: () -> Unit,
    onAddEmi: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        onClick = onLoanClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, Color(0xFF87CEFA))
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
                            text = "₹${NumberHelper.formatMoney(loan.loanAmount)} → ₹${NumberHelper.formatMoney(loan.remainingAmount)} due",
                            style = MaterialTheme.typography.titleSmall,
                            color = Color(0xFFF44336),
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
                DeleteActionButton(onClick = onDeleteClick)
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

                    if (emis.isEmpty()) {
                        Text(
                            text = "No EMI payments yet",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    } else {
                        Text(text = "EMI Payments:", style = MaterialTheme.typography.labelLarge)
                        Spacer(modifier = Modifier.height(8.dp))
                        emis.forEach { emi -> EmiRow(emi) }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(onClick = onAddEmi, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add EMI Payment")
                    }
                }
            }
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

@Composable
fun EmiRow(emi: Emi) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "EMI ${emi.emiNumber}", style = MaterialTheme.typography.bodyMedium)
        }
        Text(
            text = "₹${NumberHelper.formatMoney(emi.emiAmount)} (${DateHelper.formatDate(emi.emiDate)})",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
