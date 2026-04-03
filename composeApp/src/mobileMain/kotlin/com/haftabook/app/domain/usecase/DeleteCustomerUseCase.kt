package com.haftabook.app.domain.usecase

import com.haftabook.app.data.repository.CustomerRepository

/**
 * WHAT: Delete a customer and all related data
 * WHERE: Domain Layer
 */
class DeleteCustomerUseCase(
    private val customerRepository: CustomerRepository
) {
    suspend fun execute(customerId: Long) {
        customerRepository.deleteCustomerCascade(customerId)
    }
}
