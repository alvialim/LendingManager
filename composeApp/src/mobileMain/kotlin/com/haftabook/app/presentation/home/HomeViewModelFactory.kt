package com.haftabook.app.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.haftabook.app.domain.usecase.AddCustomerUseCase
import com.haftabook.app.domain.usecase.DeleteCustomerUseCase
import com.haftabook.app.domain.usecase.GetCustomersUseCase
import com.haftabook.app.domain.usecase.UpdateCustomerPhotoUseCase
import kotlin.reflect.KClass

class HomeViewModelFactory(
    private val getCustomersUseCase: GetCustomersUseCase,
    private val addCustomerUseCase: AddCustomerUseCase,
    private val updateCustomerPhotoUseCase: UpdateCustomerPhotoUseCase,
    private val deleteCustomerUseCase: DeleteCustomerUseCase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T {
        if (modelClass == HomeViewModel::class) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(
                getCustomersUseCase = getCustomersUseCase,
                addCustomerUseCase = addCustomerUseCase,
                updateCustomerPhotoUseCase = updateCustomerPhotoUseCase,
                deleteCustomerUseCase = deleteCustomerUseCase,
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
