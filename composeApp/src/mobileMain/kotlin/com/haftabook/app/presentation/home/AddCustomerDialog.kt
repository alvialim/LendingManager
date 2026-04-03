package com.haftabook.app.presentation.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * WHAT: Dialog to add customer
 * WHERE: Presentation Layer
 * WHY: Reusable UI component
 * 
 * SOLID: Single Responsibility
 * - Only handles dialog UI
 * - Doesn't validate or save data
 */
@Composable
fun AddCustomerDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String) -> Unit,
    errorMessage: String?
) {
    var name by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }
    var loanType by remember { mutableStateOf("MONTHLY") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = { Text("Add Customer") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Show error if any
                if (errorMessage != null) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                
                // Name input
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF87CEFA),
                        unfocusedBorderColor = Color(0xFF87CEFA),
                        disabledBorderColor = Color(0xFF87CEFA),
                        errorBorderColor = MaterialTheme.colorScheme.error,
                    )
                )
                
                // Mobile input
                OutlinedTextField(
                    value = mobile,
                    onValueChange = { 
                        if (it.length <= 10 && it.all { char -> char.isDigit() }) {
                            mobile = it
                        }
                    },
                    label = { Text("Mobile") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF87CEFA),
                        unfocusedBorderColor = Color(0xFF87CEFA),
                        disabledBorderColor = Color(0xFF87CEFA),
                        errorBorderColor = MaterialTheme.colorScheme.error,
                    )
                )
                
                // Loan type selection
                Text("Loan Type:")
                Row (verticalAlignment =Alignment.CenterVertically){

                    RadioButton(
                        selected = loanType == "MONTHLY",
                        onClick = { loanType = "MONTHLY" }
                    )
                    Text("Monthly")
                    Spacer(Modifier.width(16.dp))

                    RadioButton(
                        selected = loanType == "DAILY",
                        onClick = { loanType = "DAILY" }
                    )
                    Text("Daily")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(name, mobile, loanType) }
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

/**
 * Benefits:
 * ✅ Reusable component
 * ✅ No business logic inside
 * ✅ Just collects input and sends to ViewModel
 */