package com.haftabook.app.presentation.customer

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.haftabook.app.utils.DateHelper
import com.haftabook.app.presentation.components.DatePickerDialog
import kotlin.math.ceil

@Composable
fun AddLoanDialog(
    loanType: String,
    onDismiss: () -> Unit,
    onConfirm: (Long, Long, Long, Int) -> Unit,
    errorMessage: String?
) {
    var amount by remember { mutableStateOf("") }
    var loanStartDate by remember { mutableStateOf(DateHelper.now()) }
    var emiStartDate by remember { mutableStateOf(DateHelper.now()) }
    var totalEmis by remember { mutableStateOf("") }
    var showLoanDatePicker by remember { mutableStateOf(false) }
    var showEmiDatePicker by remember { mutableStateOf(false) }

    val lastEmiDate = remember(emiStartDate, totalEmis, loanType) {
        val emis = totalEmis.toIntOrNull() ?: 0
        if (emis > 0) {
            when (loanType) {
                "DAILY" -> DateHelper.addDays(emiStartDate, emis - 1)
                "MONTHLY" -> DateHelper.addMonths(emiStartDate, emis - 1)
                else -> emiStartDate
            }
        } else {
            emiStartDate
        }
    }

    val emiAmount = remember(amount, totalEmis) {
        val amt = amount.toDoubleOrNull() ?: 0.0
        val emis = totalEmis.toIntOrNull() ?: 0
        if (emis > 0) ceil(amt / emis).toLong() else 0L
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = { Text("Add Loan") },
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

                OutlinedTextField(
                    value = amount,
                    onValueChange = {
                        if (it.isEmpty() || it.all { char -> char.isDigit() }) {
                            amount = it
                        }
                    },
                    label = { Text("Loan Amount (₹)") },
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
                    value = DateHelper.formatDate(loanStartDate),
                    onValueChange = {},
                    label = { Text("Loan Start Date") },
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { showLoanDatePicker = true }) {
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

                OutlinedTextField(
                    value = DateHelper.formatDate(emiStartDate),
                    onValueChange = {},
                    label = { Text("EMI Start Date") },
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { showEmiDatePicker = true }) {
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

                OutlinedTextField(
                    value = totalEmis,
                    onValueChange = {
                        if (it.isEmpty() || it.all { char -> char.isDigit() }) {
                            totalEmis = it
                        }
                    },
                    label = { Text("Total EMIs") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF87CEFA),
                        unfocusedBorderColor = Color(0xFF87CEFA),
                        disabledBorderColor = Color(0xFF87CEFA),
                        errorBorderColor = MaterialTheme.colorScheme.error,
                    )
                )

                if (totalEmis.isNotEmpty() && totalEmis.toIntOrNull() != null && totalEmis.toInt() > 0) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "EMI Amount: ₹$emiAmount",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            text = "Last EMI Date: ${DateHelper.formatDate(lastEmiDate)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amount.toLongOrNull()
                    val emis = totalEmis.toIntOrNull()
                    if (amt != null && amt > 0 && emis != null && emis > 0) {
                        onConfirm(amt, loanStartDate, emiStartDate, emis)
                    }
                }
            ) {
                Text("Add Loan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )

    if (showLoanDatePicker) {
        DatePickerDialog(
            initialDate = loanStartDate,
            onDateSelected = { loanStartDate = it },
            onDismiss = { showLoanDatePicker = false }
        )
    }

    if (showEmiDatePicker) {
        DatePickerDialog(
            initialDate = emiStartDate,
            onDateSelected = { emiStartDate = it },
            onDismiss = { showEmiDatePicker = false }
        )
    }
}
