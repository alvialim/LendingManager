package com.haftabook.app.presentation.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Sms
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.haftabook.app.domain.model.Customer
import com.haftabook.app.presentation.components.CustomerCardActionButton
import com.haftabook.app.presentation.components.CustomerCardActionHeight
import com.haftabook.app.presentation.components.CustomerProgressBar
import com.haftabook.app.presentation.components.DeleteActionButton
import com.haftabook.app.presentation.theme.accentBorderForCustomer
import com.haftabook.app.presentation.theme.lightActionButtonBackground
import com.haftabook.app.ui.FabBlue
import com.haftabook.app.ui.PaidAmountGreen
import com.haftabook.app.utils.CommunicationHelper
import com.haftabook.app.utils.NumberHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onOpenDrawer: () -> Unit,
    onCustomerClick: (Long) -> Unit
) {
    val customers by viewModel.customers.collectAsState()
    val tabTotals by viewModel.tabTotals.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    var isSearchActive by remember { mutableStateOf(false) }
    var customerToDelete by remember { mutableStateOf<Customer?>(null) }

    Scaffold(
        topBar = {
            if (isSearchActive) {
                SearchTopBar(
                    totals = tabTotals,
                    query = searchQuery,
                    onQueryChange = { viewModel.onSearchQueryChange(it) },
                    onOpenDrawer = onOpenDrawer,
                    onCloseClick = {
                        isSearchActive = false
                        viewModel.onSearchQueryChange("")
                    }
                )
            } else {
                TopAppBar(
                    title = { HomeToolbarTotalsRow(totals = tabTotals) },
                    navigationIcon = {
                        IconButton(onClick = onOpenDrawer) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    },
                    actions = {
                        IconButton(onClick = { isSearchActive = true }) {
                            Icon(Icons.Default.Search, contentDescription = "Search")
                        }
                    }
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.onAddCustomerClick() },
                containerColor = FabBlue,
                contentColor = Color.White,
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.White)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { viewModel.onTabChanged(0) },
                    text = { Text("Monthly") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { viewModel.onTabChanged(1) },
                    text = { Text("Daily") }
                )
            }

            PullToRefreshBox(
                isRefreshing = viewModel.isRefreshing,
                onRefresh = {
                    viewModel.onPullRefresh()
                    isSearchActive = false
                },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                CustomerList(
                    customers = customers,
                    onCustomerClick = { onCustomerClick(it.id) },
                    onDeleteClick = { customerToDelete = it },
                    onSendMessage = { CommunicationHelper.sendCustomerMessage(it) },
                    onSendSms = { CommunicationHelper.openSmsToAdminWithCustomer(it) }
                )
            }
        }
    }
    
    // Delete Confirmation
    customerToDelete?.let { customer ->
        AlertDialog(
            onDismissRequest = { customerToDelete = null },
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Delete Customer?") },
            text = { Text("This will delete ${customer.name} and all their loans. This cannot be undone.") },
            confirmButton = {
                DeleteActionButton(
                    onClick = {
                        viewModel.onDeleteCustomerClick(customer.id)
                        customerToDelete = null
                    }
                )
            },
            dismissButton = {
                TextButton(onClick = { customerToDelete = null }) { Text("Cancel") }
            }
        )
    }

    if (viewModel.showAddDialog) {
        AddCustomerDialog(
            onDismiss = { viewModel.onDismissDialog() },
            onConfirm = { name, mobile ->
                viewModel.onAddCustomer(name, mobile)
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
}

@Composable
fun HomeToolbarTotalsRow(
    totals: HomeTabTotals,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        ToolbarTotalColumn(
            label = "Given",
            amount = totals.totalGiven,
            amountColor = MaterialTheme.colorScheme.onSurface
        )
        ToolbarTotalColumn(
            label = "Paid",
            amount = totals.totalPaid,
            amountColor = PaidAmountGreen
        )
        ToolbarTotalColumn(
            label = "Due",
            amount = totals.totalDue,
            amountColor = Color(0xFFF44336)
        )
    }
}

@Composable
private fun ToolbarTotalColumn(
    label: String,
    amount: Long,
    amountColor: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = "₹${NumberHelper.formatMoney(amount)}",
            style = MaterialTheme.typography.labelMedium,
            color = amountColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchTopBar(
    totals: HomeTabTotals,
    query: String,
    onQueryChange: (String) -> Unit,
    onOpenDrawer: () -> Unit,
    onCloseClick: () -> Unit
) {
    TopAppBar(
        title = {
            Column(modifier = Modifier.fillMaxWidth()) {
                TextField(
                    value = query,
                    onValueChange = onQueryChange,
                    placeholder = { Text("Search name, number, amount...") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                    ),
                    singleLine = true
                )
                HomeToolbarTotalsRow(
                    totals = totals,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                )
            }
        },
        navigationIcon = {
            Row {
                IconButton(onClick = onOpenDrawer) {
                    Icon(Icons.Default.Menu, contentDescription = "Menu")
                }
                IconButton(onClick = onCloseClick) {
                    Icon(Icons.Default.Close, contentDescription = "Close Search")
                }
            }
        }
    )
}

@Composable
fun CustomerList(
    customers: List<Customer>,
    onCustomerClick: (Customer) -> Unit,
    onDeleteClick: (Customer) -> Unit,
    onSendMessage: (Customer) -> Unit,
    onSendSms: (Customer) -> Unit,
) {
    val click by rememberUpdatedState(onCustomerClick)
    val delete by rememberUpdatedState(onDeleteClick)
    val sendMessage by rememberUpdatedState(onSendMessage)
    val sendSms by rememberUpdatedState(onSendSms)
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (customers.isEmpty()) {
            item(contentType = "empty") {
                Box(
                    modifier = Modifier
                        .fillParentMaxSize()
                        .heightIn(min = 240.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No customers found")
                }
            }
        } else {
            items(
                items = customers,
                key = { it.id },
                contentType = { "customer" }
            ) { customer ->
                CustomerCard(
                    customer = customer,
                    onClick = { click(customer) },
                    onDelete = { delete(customer) },
                    onSendMessage = { sendMessage(customer) },
                    onSendSms = { sendSms(customer) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerCard(
    customer: Customer,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onSendMessage: () -> Unit,
    onSendSms: () -> Unit,
) {
    val givenStr = remember(customer.totalGiven) { NumberHelper.formatMoney(customer.totalGiven) }
    val paidStr = remember(customer.totalPaid) { NumberHelper.formatMoney(customer.totalPaid) }
    val dueStr = remember(customer.totalDue) { NumberHelper.formatMoney(customer.totalDue) }
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, Color(0xFFE6F1FC))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = customer.name, style = MaterialTheme.typography.titleLarge)
                    Text(text = customer.mobile, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.height(CustomerCardActionHeight)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${customer.totalLoans} Loans",
                            style = MaterialTheme.typography.labelLarge,
                            )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Column {
                CustomerProgressBar(customer = customer)

                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("Given", style = MaterialTheme.typography.labelSmall)
                        Text("₹$givenStr", style = MaterialTheme.typography.bodyLarge)
                    }
                    Column {
                        Text("Paid", style = MaterialTheme.typography.labelSmall)
                        Text("₹$paidStr", style = MaterialTheme.typography.bodyLarge, color = PaidAmountGreen)
                    }
                    Column {
                        Text("Due", style = MaterialTheme.typography.labelSmall)
                        Text("₹$dueStr", style = MaterialTheme.typography.bodyLarge, color = Color(0xFFF44336))
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CustomerCardActionButton(
                    icon = Icons.Outlined.Share,
                    contentDescription = "Share",
                    onClick = onSendMessage,
                    backgroundColor =     Color(0xFFB0DBFF),

                )
                Spacer(modifier = Modifier.width(4.dp))
                CustomerCardActionButton(
                    icon = Icons.Outlined.Sms,
                    contentDescription = "SMS",
                    onClick = onSendSms,
                    backgroundColor =     Color(0xFFC6FFC8),

                )
                Spacer(modifier = Modifier.width(4.dp))
                CustomerCardActionButton(
                    icon = Icons.Outlined.Delete,
                    contentDescription = "Delete",
                    onClick = onDelete,
                    backgroundColor =     Color(0xFFFFB7CC),

                )
            }
        }
    }
}
