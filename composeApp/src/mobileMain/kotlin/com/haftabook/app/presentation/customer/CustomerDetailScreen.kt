package com.haftabook.app.presentation.customer

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.haftabook.app.domain.model.Customer
import com.haftabook.app.domain.model.Loan
import com.haftabook.app.utils.NumberHelper
import com.haftabook.app.presentation.components.CustomerProgressBar
import com.haftabook.app.platform.rememberCustomerPhotoPicker
import com.haftabook.app.presentation.components.CustomerAvatar
import com.haftabook.app.presentation.components.DeleteActionButton
import com.haftabook.app.ui.FabBlue
import com.haftabook.app.ui.PaidAmountGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerDetailScreen(
    viewModel: CustomerDetailViewModel,
    onBack: () -> Unit
) {
    val customer = viewModel.customer
    var showPhotoPicker by remember { mutableStateOf(false) }
    var photoError by remember { mutableStateOf<String?>(null) }
    val picker = rememberCustomerPhotoPicker(
        onImageBytes = { bytes ->
            photoError = null
            showPhotoPicker = false
            viewModel.onUpdateCustomerPhoto(bytes)
        },
        onError = { msg ->
            photoError = msg
            showPhotoPicker = false
        }
    )
    val loans by viewModel.loans.collectAsState(initial = emptyList())
    val expandedLoanEmis by viewModel.expandedLoanEmis
    var loanToDelete by remember { mutableStateOf<Loan?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CustomerAvatar(
                                photoPath = customer?.photoPath,
                                displayName = customer?.name,
                                modifier = Modifier,
                                onClick = { showPhotoPicker = true }
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(customer?.name ?: "Loading...")
                                Text(
                                    text = customer?.mobile ?: "",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                        Text(
                            text = photoError ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        },
        floatingActionButtonPosition = FabPosition.Center,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.onAddLoanClick() },
                containerColor = Color(0xFF16A34A),
                contentColor = Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)

            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Loan", tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Loan")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding),
            contentPadding = PaddingValues(
                start = 16.dp,
                top = 16.dp,
                end = 16.dp,
                bottom = 96.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                customer?.let { CustomerSummaryCard(it) }
            }

            viewModel.errorMessage?.let { err ->
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = err,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.weight(1f)
                            )
                            TextButton(onClick = { viewModel.clearErrorMessage() }) {
                                Text("OK")
                            }
                        }
                    }
                }
            }

            if (loans.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        border = BorderStroke(1.dp, Color(0xFF87CEFA))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = androidx.compose.ui.Alignment.Center
                        ) {
                            Text("No loans yet\nClick + to add loan")
                        }
                    }
                }
            } else {
                items(loans) { loan ->
                    LoanCard(
                        loan = loan,
                        customerName = customer?.name.orEmpty(),
                        customerMobile = customer?.mobile.orEmpty(),
                        loanType = customer?.loanType ?: "MONTHLY",
                        isExpanded = viewModel.expandedLoanId == loan.id,
                        emis = if (viewModel.expandedLoanId == loan.id) expandedLoanEmis else emptyList(),
                        onLoanClick = { viewModel.onLoanClick(loan.id) },
                        onDeleteClick = { loanToDelete = loan },
                        onMarkEmiSlotPaid = { emiNum ->
                            viewModel.onMarkEmiSlotPaid(loan.id, emiNum)
                        }
                    )
                }
            }
        }
    }

    // Loan Delete Confirmation
    loanToDelete?.let { loan ->
        AlertDialog(
            onDismissRequest = { loanToDelete = null },
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Delete Loan ${loan.loanNumber}?") },
            text = { Text("This will delete this loan and all its EMI payments. This cannot be undone.") },
            confirmButton = {
                DeleteActionButton(
                    onClick = {
                        viewModel.onDeleteLoanClick(loan.id)
                        loanToDelete = null
                    }
                )
            },
            dismissButton = {
                TextButton(onClick = { loanToDelete = null }) { Text("Cancel") }
            }
        )
    }

    if (viewModel.showAddLoanDialog) {
        AddLoanDialog(
            loanType = customer?.loanType ?: "MONTHLY",
            onDismiss = { viewModel.onDismissDialog() },
            onConfirm = { amount, startDate, emiStartDate, totalEmis ->
                viewModel.onAddLoan(amount, startDate, emiStartDate, totalEmis)
            },
            errorMessage = viewModel.errorMessage
        )
    }

    if (viewModel.isLoading) {
        AlertDialog(
            onDismissRequest = { },
            confirmButton = {},
            title = { Text("Please wait") },
            text = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        )
    }

    if (showPhotoPicker) {
        AlertDialog(
            onDismissRequest = { showPhotoPicker = false },
            title = { Text("Update photo") },
            text = { Text("Choose a new profile photo") },
            confirmButton = {
                TextButton(onClick = { picker.pickFromGallery() }) { Text("Gallery") }
            },
            dismissButton = {
                TextButton(onClick = { picker.captureFromCamera() }) { Text("Camera") }
            }
        )
    }
}

@Composable
fun CustomerSummaryCard(customer: Customer) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = BorderStroke(1.dp, Color(0xFFDBEDF6))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Total Given", style = MaterialTheme.typography.labelSmall)
                    Text(
                        "₹${NumberHelper.formatMoney(customer.totalGiven)}",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF2196F3)
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Total Paid", style = MaterialTheme.typography.labelSmall)
                    Text(
                        "₹${NumberHelper.formatMoney(customer.totalPaid)}",
                        style = MaterialTheme.typography.titleMedium,
                        color = PaidAmountGreen
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.End
                ) {
                    Text("Total Due", style = MaterialTheme.typography.labelSmall)
                    Text(
                        "₹${NumberHelper.formatMoney(customer.totalDue)}",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFFF44336)
                    )
                }
            }

            CustomerProgressBar(customer = customer)
        }
    }
}
