package com.haftabook.app.domain.usecase

import com.haftabook.app.data.repository.CustomerRepository

class UpdateCustomerPhotoUseCase(
    private val customerRepository: CustomerRepository
) {
    suspend fun execute(customerId: Long, photoPath: String?): Result<Unit> = runCatching {
        customerRepository.updateCustomerPhotoPath(customerId, photoPath)
    }
}

