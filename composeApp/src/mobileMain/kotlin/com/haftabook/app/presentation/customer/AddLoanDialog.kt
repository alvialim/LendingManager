package com.haftabook.app.presentation.customer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.haftabook.app.ui.PaidAmountGreen
import com.haftabook.app.utils.DateHelper
import com.haftabook.app.presentation.components.DatePickerDialog
import kotlin.math.ceil

@Composable
fun AddLoanDialog(
    loanType: String,
    onDismiss: () -> Unit,
    onConfirm: (amount: Long, interest: Long?, loanStartDate: Long, emiStartDate: Long, totalEmis: Int) -> Unit,
    errorMessage: String?
) {
    var amount by remember { mutableStateOf("") }
    var interest by remember { mutableStateOf("") }
    val dialogBaselineLoanStart = remember { DateHelper.now() }
    var loanStartDate by remember { mutableStateOf(dialogBaselineLoanStart) }
    var emiStartDate by remember {
        mutableStateOf(DateHelper.firstEmiDateFromLoanStart(dialogBaselineLoanStart, loanType))
    }
    var totalEmis by remember { mutableStateOf("") } // DAILY only; MONTHLY is fixed 12
    var showLoanDatePicker by remember { mutableStateOf(false) }
    var showEmiDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(loanStartDate, loanType) {
        emiStartDate = DateHelper.firstEmiDateFromLoanStart(loanStartDate, loanType)
    }

    val lastEmiDate = remember(emiStartDate, totalEmis, loanType) {
        val emis = if (loanType == "MONTHLY") 12 else (totalEmis.toIntOrNull() ?: 0)
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

    val emiAmount = remember(loanType, amount, interest, totalEmis) {
        if (loanType == "MONTHLY") {
            interest.toLongOrNull() ?: 0L
        } else {
            val amt = amount.toDoubleOrNull() ?: 0.0
            val emis = totalEmis.toIntOrNull() ?: 0
            if (emis > 0) ceil(amt / emis).toLong() else 0L
        }
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
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF87CEFA),
                        unfocusedBorderColor = Color(0xFF87CEFA),
                        disabledBorderColor = Color(0xFF87CEFA),
                        errorBorderColor = MaterialTheme.colorScheme.error,
                    )
                )

                if (loanType == "MONTHLY") {
                    OutlinedTextField(
                        value = interest,
                        onValueChange = {
                            if (it.isEmpty() || it.all { char -> char.isDigit() }) {
                                interest = it
                            }
                        },
                        label = { Text("Interest (₹)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF87CEFA),
                            unfocusedBorderColor = Color(0xFF87CEFA),
                            disabledBorderColor = Color(0xFF87CEFA),
                            errorBorderColor = MaterialTheme.colorScheme.error,
                        )
                    )
                }

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
                    label = { Text("First EMI Date") },
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

                if (loanType != "MONTHLY") {
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
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF87CEFA),
                            unfocusedBorderColor = Color(0xFF87CEFA),
                            disabledBorderColor = Color(0xFF87CEFA),
                            errorBorderColor = MaterialTheme.colorScheme.error,
                        )
                    )
                } else {
                    OutlinedTextField(
                        value = "12",
                        onValueChange = {},
                        label = { Text("Total EMIs") },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF87CEFA),
                            unfocusedBorderColor = Color(0xFF87CEFA),
                            disabledBorderColor = Color(0xFF87CEFA),
                            errorBorderColor = MaterialTheme.colorScheme.error,
                        )
                    )
                }

                val showPreview = if (loanType == "MONTHLY") {
                    (interest.toLongOrNull() ?: 0L) > 0L
                } else {
                    totalEmis.isNotEmpty() && totalEmis.toIntOrNull() != null && totalEmis.toInt() > 0
                }
                if (showPreview) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "EMI Amount: ₹$emiAmount",
                            style = MaterialTheme.typography.bodyLarge,
                            color = PaidAmountGreen
                        )
                        Text(
                            text = "Last EMI Date: ${DateHelper.formatDate(lastEmiDate)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amount.toLongOrNull()
                    val isMonthly = loanType == "MONTHLY"
                    val emis = if (isMonthly) 12 else totalEmis.toIntOrNull()
                    val interestAmt = if (isMonthly) interest.toLongOrNull() else null
                    if (amt != null && amt > 0 && emis != null && emis > 0) {
                        if (!isMonthly || (interestAmt != null && interestAmt > 0L)) {
                            onConfirm(amt, interestAmt, loanStartDate, emiStartDate, emis)
                        }
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
