package com.haftabook.app.presentation.customer

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.haftabook.app.domain.model.Customer
import com.haftabook.app.domain.model.Emi
import com.haftabook.app.domain.model.Loan
import com.haftabook.app.domain.usecase.AddLoanUseCase
import com.haftabook.app.domain.usecase.DeleteLoanUseCase
import com.haftabook.app.domain.usecase.GetCustomerDetailsUseCase
import com.haftabook.app.domain.usecase.GetEmisUseCase
import com.haftabook.app.domain.usecase.GetLoansUseCase
import com.haftabook.app.domain.usecase.MarkEmiSlotPaidUseCase
import com.haftabook.app.utils.CommunicationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CustomerDetailViewModel(
    private val customerId: Long,
    private val getCustomerDetailsUseCase: GetCustomerDetailsUseCase,
    private val getLoansUseCase: GetLoansUseCase,
    private val getEmisUseCase: GetEmisUseCase,
    private val addLoanUseCase: AddLoanUseCase,
    private val markEmiSlotPaidUseCase: MarkEmiSlotPaidUseCase,
    private val deleteLoanUseCase: DeleteLoanUseCase
) : ViewModel() {

    var customer by mutableStateOf<Customer?>(null)
    var showAddLoanDialog by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var isLoading by mutableStateOf(false)

    var expandedLoanId by mutableStateOf<Long?>(null)
    val expandedLoanEmis = mutableStateOf<List<Emi>>(emptyList())

    private var emisCollectionJob: Job? = null

    val loans: Flow<List<Loan>> = getLoansUseCase.execute(customerId)

    init {
        loadCustomer()
    }

    private fun loadCustomer() {
        viewModelScope.launch {
            isLoading = true
            val loaded = withContext(Dispatchers.IO) {
                getCustomerDetailsUseCase.execute(customerId)
            }
            customer = loaded
            isLoading = false
        }
    }

    private fun restartEmiCollection(loanId: Long) {
        emisCollectionJob?.cancel()
        emisCollectionJob = viewModelScope.launch(Dispatchers.Default) {
            getEmisUseCase.execute(loanId).collect { emis ->
                withContext(Dispatchers.Main) {
                    expandedLoanEmis.value = emis
                }
            }
        }
    }

    fun onLoanClick(loanId: Long) {
        emisCollectionJob?.cancel()
        if (expandedLoanId == loanId) {
            expandedLoanId = null
            expandedLoanEmis.value = emptyList()
            return
        }
        expandedLoanId = loanId
        restartEmiCollection(loanId)
    }

    fun onDeleteLoanClick(loanId: Long) {
        viewModelScope.launch {
            isLoading = true
            withContext(Dispatchers.IO) {
                deleteLoanUseCase.execute(loanId)
            }
            loadCustomer()
            isLoading = false
        }
    }

    fun onAddLoanClick() { showAddLoanDialog = true }

    fun onAddLoan(
        amount: Long,
        loanStartDate: Long,
        emiStartDate: Long,
        totalEmis: Int
    ) {
        viewModelScope.launch {
            isLoading = true
            val result = withContext(Dispatchers.IO) {
                addLoanUseCase.execute(
                    customerId = customerId,
                    loanType = customer?.loanType ?: "MONTHLY",
                    amount = amount,
                    loanStartDate = loanStartDate,
                    emiStartDate = emiStartDate,
                    totalEmis = totalEmis
                )
            }

            if (result.isSuccess) {
                showAddLoanDialog = false
                errorMessage = null
                loadCustomer()
                CommunicationHelper.sendLoanAddedMessages(customer?.name ?: "Unknown", amount)
            } else {
                errorMessage = result.exceptionOrNull()?.message
            }
            isLoading = false
        }
    }

    fun onMarkEmiSlotPaid(loanId: Long, emiNumber: Int) {
        viewModelScope.launch {
            isLoading = true
            val result = withContext(Dispatchers.IO) {
                markEmiSlotPaidUseCase.execute(
                    loanId,
                    emiNumber,
                    customer?.loanType ?: "MONTHLY"
                )
            }
            if (result.isSuccess) {
                errorMessage = null
                val paidAmount = result.getOrNull() ?: 0L
                loadCustomer()
                if (expandedLoanId == loanId) {
                    restartEmiCollection(loanId)
                }
                CommunicationHelper.sendEmiAddedMessages(
                    customer?.name ?: "Unknown",
                    paidAmount,
                    emiNumber
                )
            } else {
                errorMessage = result.exceptionOrNull()?.message
            }
            isLoading = false
        }
    }

    fun onDismissDialog() {
        showAddLoanDialog = false
        errorMessage = null
    }

    fun clearErrorMessage() {
        errorMessage = null
    }

    override fun onCleared() {
        emisCollectionJob?.cancel()
        super.onCleared()
    }
}
