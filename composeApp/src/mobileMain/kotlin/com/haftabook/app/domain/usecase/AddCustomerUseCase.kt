package com.haftabook.app.domain.usecase

import com.haftabook.app.data.local.entity.CustomerEntity
import com.haftabook.app.data.repository.CustomerRepository
import com.haftabook.app.util.currentTimeMillis

/**
 * WHAT: Add new customer
 * WHERE: Domain Layer (Clean Architecture)
 * WHY: Validation and business rules
 */
class AddCustomerUseCase(
    private val repository: CustomerRepository
) {
    suspend fun execute(
        name: String,
        mobile: String,
        loanType: String
    ): Result<Long> {
        // Validation
        if (name.isBlank()) {
            return Result.failure(Exception("Name cannot be empty"))
        }
        
        if (mobile.length != 10) {
            return Result.failure(Exception("Mobile must be 10 digits"))
        }
        
        // Create entity and save via repository
        // repository.addCustomer handles remoteId generation and sync outbox
        val entity = CustomerEntity(
            name = name,
            mobile = mobile,
            loanType = loanType,
            createdDate = currentTimeMillis()
        )
        
        val id = repository.addCustomer(entity)
        return Result.success(id)
    }
}
