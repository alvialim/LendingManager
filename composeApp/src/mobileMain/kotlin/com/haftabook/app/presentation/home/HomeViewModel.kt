package com.haftabook.app.presentation.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.haftabook.app.domain.model.Customer
import com.haftabook.app.domain.usecase.AddCustomerUseCase
import com.haftabook.app.domain.usecase.DeleteCustomerUseCase
import com.haftabook.app.domain.usecase.GetCustomersUseCase
import com.haftabook.app.domain.usecase.UpdateCustomerPhotoUseCase
import com.haftabook.app.platform.saveCustomerProfilePhoto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * WHAT: ViewModel for Home Screen with Delete functionality
 */
data class HomeTabTotals(
    val totalGiven: Long,
    val totalPaid: Long,
    val totalDue: Long
)

class HomeViewModel(
    private val loanTypeFilter: String,
    private val getCustomersUseCase: GetCustomersUseCase,
    private val addCustomerUseCase: AddCustomerUseCase,
    private val updateCustomerPhotoUseCase: UpdateCustomerPhotoUseCase,
    private val deleteCustomerUseCase: DeleteCustomerUseCase,
    private val requestSyncNow: suspend () -> Unit = {},
) : ViewModel() {
    private val searchQueryFlow = MutableStateFlow("")

    /**
     * Single source for “all customers” in the UI. Room’s Flow can emit a stale list right after
     * an insert (especially offline); we drop those emissions until they include [pendingNewCustomerId].
     */
    private val customerListRaw = MutableStateFlow<List<Customer>>(emptyList())

    @Volatile
    private var pendingNewCustomerId: Long? = null

    @Volatile
    private var pendingDeletedCustomerId: Long? = null

    init {
        viewModelScope.launch(Dispatchers.Default) {
            getCustomersUseCase.observeAllCustomers().collect { room ->
                val pin = pendingNewCustomerId
                if (pin != null && room.none { it.id == pin }) {
                    return@collect
                }
                if (pin != null && room.any { it.id == pin }) {
                    pendingNewCustomerId = null
                }
                val del = pendingDeletedCustomerId
                if (del != null && room.any { it.id == del }) {
                    return@collect
                }
                if (del != null && room.none { it.id == del }) {
                    pendingDeletedCustomerId = null
                }
                customerListRaw.value = room
            }
        }
    }

    val searchQuery: StateFlow<String> = searchQueryFlow.asStateFlow()

    var showAddDialog by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var isLoading by mutableStateOf(false)
    var isRefreshing by mutableStateOf(false)

    val customers: StateFlow<List<Customer>> = combine(
        searchQueryFlow,
        customerListRaw
    ) { query, allCustomers ->
        val forTab = allCustomers.filter { it.loanType == loanTypeFilter }
        if (query.isBlank()) forTab
        else forTab.filter { customer ->
            customer.name.contains(query, ignoreCase = true) ||
                customer.mobile.contains(query) ||
                customer.totalGiven.toString().contains(query) ||
                customer.totalPaid.toString().contains(query) ||
                customer.totalDue.toString().contains(query)
        }
    }.flowOn(Dispatchers.Default).stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptyList()
    )

    val tabTotals: StateFlow<HomeTabTotals> = customerListRaw.map { allCustomers ->
        val list = allCustomers.filter { it.loanType == loanTypeFilter }
        HomeTabTotals(
            totalGiven = list.sumOf { it.totalGiven },
            totalPaid = list.sumOf { it.totalPaid },
            totalDue = list.sumOf { it.totalDue }
        )
    }.flowOn(Dispatchers.Default).stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = HomeTabTotals(0L, 0L, 0L)
    )

    fun onSearchQueryChange(newQuery: String) {
        searchQueryFlow.value = newQuery
    }

    /** Clears search so the full list for the current tab (Monthly or Daily) is shown. */
    fun onPullRefresh() {
        viewModelScope.launch {
            isRefreshing = true
            searchQueryFlow.value = ""
            withContext(Dispatchers.IO) {
                runCatching { requestSyncNow() }
                delay(350)
            }
            isRefreshing = false
        }
    }

    fun onAddCustomerClick() { showAddDialog = true }

    fun onDeleteCustomerClick(customerId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            pendingDeletedCustomerId = customerId
            deleteCustomerUseCase.execute(customerId)
            val fresh = getCustomersUseCase.loadAllCustomersSnapshot()
            customerListRaw.value = fresh
        }
    }

    fun onAddCustomer(name: String, mobile: String, photoBytes: ByteArray?) {
        val loanType = loanTypeFilter
        viewModelScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) { isLoading = true }
            try {
                val result = addCustomerUseCase.execute(name, mobile, loanType)
                if (result.isSuccess) {
                    val newId = result.getOrNull()!!
                    if (photoBytes != null) {
                        val path = saveCustomerProfilePhoto(newId, photoBytes)
                        updateCustomerPhotoUseCase.execute(newId, path)
                    }
                    pendingNewCustomerId = newId
                    val fresh = getCustomersUseCase.loadAllCustomersSnapshot()
                    customerListRaw.value = fresh
                    withContext(Dispatchers.Main) {
                        showAddDialog = false
                        errorMessage = null
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        errorMessage = result.exceptionOrNull()?.message
                    }
                }
            } finally {
                withContext(Dispatchers.Main) { isLoading = false }
            }
        }
    }

    fun onDismissDialog() {
        showAddDialog = false
        errorMessage = null
    }
}
