package com.haftabook.app.presentation.customer

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.haftabook.app.domain.model.Customer
import com.haftabook.app.domain.model.Loan
import com.haftabook.app.utils.NumberHelper
import com.haftabook.app.presentation.components.DeleteActionButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerDetailScreen(
    viewModel: CustomerDetailViewModel,
    onBack: () -> Unit
) {
    val customer = viewModel.customer
    val loans by viewModel.loans.collectAsState(initial = emptyList())
    val expandedLoanEmis by viewModel.expandedLoanEmis
    var loanToDelete by remember { mutableStateOf<Loan?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(customer?.name ?: "Loading...")
                        Text(
                            text = customer?.mobile ?: "",
                            style = MaterialTheme.typography.bodyMedium
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
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.onAddLoanClick() }
            ) {
                Icon(Icons.Default.Add, "Add Loan")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                customer?.let { CustomerSummaryCard(it) }
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
                        isExpanded = viewModel.expandedLoanId == loan.id,
                        emis = if (viewModel.expandedLoanId == loan.id) expandedLoanEmis else emptyList(),
                        onLoanClick = { viewModel.onLoanClick(loan.id) },
                        onAddEmi = { viewModel.onAddEmiClick(loan.id) },
                        onDeleteClick = { loanToDelete = loan }
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

    if (viewModel.showAddEmiDialog && viewModel.selectedLoanId != null) {
        val selectedLoan = loans.find { it.id == viewModel.selectedLoanId }
        selectedLoan?.let { loan ->
            AddEmiDialog(
                loan = loan,
                loanType = customer?.loanType ?: "MONTHLY",
                emiNumber = expandedLoanEmis.size + 1,
                onDismiss = { viewModel.onDismissDialog() },
                onConfirm = { amount, date ->
                    viewModel.onAddEmi(amount, date)
                },
                errorMessage = viewModel.errorMessage
            )
        }
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
}

@Composable
fun CustomerSummaryCard(customer: Customer) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = BorderStroke(1.dp, Color(0xFF87CEFA))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Total Given", style = MaterialTheme.typography.labelMedium)
                    Text(
                        "₹${NumberHelper.formatMoney(customer.totalGiven)}",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color(0xFF2196F3)
                    )
                }
                Column {
                    Text("Total Paid", style = MaterialTheme.typography.labelSmall)
                    Text(
                        "₹${NumberHelper.formatMoney(customer.totalPaid)}",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color(0xFF4CAF50)
                    )
                }
            }

            HorizontalDivider()

            Column {
                Text("Total Due", style = MaterialTheme.typography.labelMedium)
                Text(
                    "₹${NumberHelper.formatMoney(customer.totalDue)}",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color(0xFFF44336)
                )
            }
        }
    }
}
