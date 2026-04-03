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
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.haftabook.app.domain.model.Customer
import com.haftabook.app.presentation.components.DeleteActionButton
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
                onClick = { viewModel.onAddCustomerClick() }
            ) {
                Icon(Icons.Default.Add, "Add")
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
                    onDeleteClick = { customerToDelete = it }
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
            onConfirm = { name, mobile, type ->
                viewModel.onAddCustomer(name, mobile, type)
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

private val CustomerProgressPaidOlive = Color(0xFF6B8E23)
private val CustomerProgressRemainingLightRed = Color(0xFFFFCDD2)
private val CustomerProgressTrackGrey = Color(0xFFE8E8E8)
private val TotalLoanChipTextBlue = Color(0xFF1976D2)
/** Same height for loan-count badge and delete control so they align vertically. */
private val CustomerCardActionHeight = 36.dp

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
            amountColor = MaterialTheme.colorScheme.primary
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
    onDeleteClick: (Customer) -> Unit
) {
    val click by rememberUpdatedState(onCustomerClick)
    val delete by rememberUpdatedState(onDeleteClick)
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
                    onDelete = { delete(customer) }
                )
            }
        }
    }
}

@Composable
private fun CustomerCardProgressBar(customer: Customer) {
    val given = customer.totalGiven
    val paid = customer.totalPaid
    val due = customer.totalDue
    val (paidF, dueF, hasPaidProgress) = remember(given, paid, due) {
        val paidF = if (given > 0) paid.toFloat() / given.toFloat() else 0f
        val dueF = if (given > 0) due.toFloat() / given.toFloat() else 0f
        val hasPaidProgress = given > 0 && paid > 0
        Triple(paidF, dueF, hasPaidProgress)
    }
    val shape = RoundedCornerShape(4.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(shape)
            .background(
                if (hasPaidProgress) CustomerProgressTrackGrey
                else CustomerProgressRemainingLightRed
            )
    ) {
        if (hasPaidProgress) {
            Row(modifier = Modifier.fillMaxSize()) {
                if (paidF > 0f) {
                    Box(
                        modifier = Modifier
                            .weight(paidF)
                            .fillMaxHeight()
                            .background(CustomerProgressPaidOlive)
                    )
                }
                if (dueF > 0f) {
                    Box(
                        modifier = Modifier
                            .weight(dueF)
                            .fillMaxHeight()
                            .background(CustomerProgressRemainingLightRed)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerCard(customer: Customer, onClick: () -> Unit, onDelete: () -> Unit) {
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
        border = BorderStroke(1.dp, Color(0xFF87CEFA))
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

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    if (customer.totalLoans > 0) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, TotalLoanChipTextBlue),
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
                                    color = TotalLoanChipTextBlue
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    DeleteActionButton(
                        onClick = onDelete,
                        modifier = Modifier.height(CustomerCardActionHeight),
                        matchChipRowSize = false,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                        shape = RoundedCornerShape(8.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            CustomerCardProgressBar(customer = customer)

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Given", style = MaterialTheme.typography.labelSmall)
                    Text("₹$givenStr", style = MaterialTheme.typography.bodyLarge)
                }
                Column {
                    Text("Paid", style = MaterialTheme.typography.labelSmall)
                    Text("₹$paidStr", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
                }
                Column {
                    Text("Due", style = MaterialTheme.typography.labelSmall)
                    Text("₹$dueStr", style = MaterialTheme.typography.bodyLarge, color = Color(0xFFF44336))
                }
            }
        }
    }
}
