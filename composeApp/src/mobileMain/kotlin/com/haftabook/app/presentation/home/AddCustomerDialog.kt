package com.haftabook.app.presentation.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.haftabook.app.platform.rememberCustomerPhotoPicker
import com.haftabook.app.presentation.components.CustomerAvatarBytes

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
    onConfirm: (String, String, ByteArray?) -> Unit,
    errorMessage: String?
) {
    var name by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }
    var photoBytes by remember { mutableStateOf<ByteArray?>(null) }
    var showPhotoPicker by remember { mutableStateOf(false) }
    var photoError by remember { mutableStateOf<String?>(null) }

    val picker = rememberCustomerPhotoPicker(
        onImageBytes = { bytes ->
            photoBytes = bytes
            photoError = null
            showPhotoPicker = false
        },
        onError = { msg ->
            photoError = msg
            showPhotoPicker = false
        }
    )

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

                if (photoError != null) {
                    Text(
                        text = photoError!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    // Optional photo picker (placeholder when no photo chosen yet).
                    CustomerAvatarBytes(
                        photoBytes = photoBytes,
                        displayName = name,
                        modifier = Modifier.size(120.dp),
                        onClick = { showPhotoPicker = true }
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
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(name, mobile, photoBytes) }
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

    if (showPhotoPicker) {
        AlertDialog(
            onDismissRequest = { showPhotoPicker = false },
            title = { Text("Customer photo") },
            text = { Text("Choose a photo (optional)") },
            confirmButton = {
                TextButton(onClick = { picker.pickFromGallery() }) { Text("Gallery") }
            },
            dismissButton = {
                TextButton(onClick = { picker.captureFromCamera() }) { Text("Camera") }
            }
        )
    }
}

/**
 * Benefits:
 * ✅ Reusable component
 * ✅ No business logic inside
 * ✅ Just collects input and sends to ViewModel
 */