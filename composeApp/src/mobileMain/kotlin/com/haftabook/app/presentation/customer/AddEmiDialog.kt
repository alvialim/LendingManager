package com.haftabook.app.presentation.customer

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.haftabook.app.domain.model.Loan
import com.haftabook.app.presentation.components.DatePickerDialog
import com.haftabook.app.utils.DateHelper
import com.haftabook.app.utils.NumberHelper

/**
 * WHAT: Dialog to add EMI payment with pre-filled details
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEmiDialog(
    loan: Loan,
    loanType: String,
    emiNumber: Int,
    onDismiss: () -> Unit,
    onConfirm: (Long, Long) -> Unit,
    errorMessage: String?
) {
    // 1. Pre-fill amount from loan
    var amount by remember { mutableStateOf(loan.emiAmount.toString()) }
    
    // 2. Calculate next EMI date based on emiNumber (e.g. 1st EMI = start, 2nd = start + 1 interval)
    val calculatedDate = remember(loan.emiStartDate, emiNumber, loanType) {
        if (emiNumber <= 1) {
            loan.emiStartDate
        } else {
            when (loanType) {
                "DAILY" -> DateHelper.addDays(loan.emiStartDate, emiNumber - 1)
                "MONTHLY" -> DateHelper.addMonths(loan.emiStartDate, emiNumber - 1)
                else -> loan.emiStartDate
            }
        }
    }
    
    var emiDate by remember { mutableStateOf(calculatedDate) }
    var showDatePicker by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = { Text("Add EMI #$emiNumber") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (errorMessage != null) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    border = BorderStroke(1.dp, Color(0xFF87CEFA))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Loan ${loan.loanNumber}", style = MaterialTheme.typography.titleSmall)
                        Text("Due: ₹${NumberHelper.formatMoney(loan.remainingAmount)}", style = MaterialTheme.typography.bodyMedium)
                    }
                }

                OutlinedTextField(
                    value = amount,
                    onValueChange = { if (it.all { char -> char.isDigit() }) amount = it },
                    label = { Text("EMI Amount (₹)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF87CEFA),
                        unfocusedBorderColor = Color(0xFF87CEFA),
                        disabledBorderColor = Color(0xFF87CEFA),
                        errorBorderColor = MaterialTheme.colorScheme.error,
                    )
                )

                OutlinedTextField(
                    value = DateHelper.formatDate(emiDate),
                    onValueChange = {},
                    label = { Text("EMI Date") },
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Default.DateRange, "Select Date")
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF87CEFA),
                        unfocusedBorderColor = Color(0xFF87CEFA),
                        disabledBorderColor = Color(0xFF87CEFA),
                        errorBorderColor = MaterialTheme.colorScheme.error,
                    )
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val amt = amount.toLongOrNull()
                if (amt != null && amt > 0) onConfirm(amt, emiDate)
            }) {
                Text("Add EMI")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )

    if (showDatePicker) {
        DatePickerDialog(
            initialDate = emiDate,
            onDateSelected = { emiDate = it },
            onDismiss = { showDatePicker = false }
        )
    }
}
