package com.haftabook.app.domain.usecase
import com.haftabook.app.data.repository.EmiRepository
import com.haftabook.app.domain.model.Emi




import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

/**
 * WHAT: Get EMIs for a loan
 * WHERE: Domain Layer
 * WHY: Load EMIs separately when needed
 *
 * SOLID: Single Responsibility
 * - Only gets EMIs for one loan
 */
class GetEmisUseCase(
    private val emiRepository: EmiRepository
) {
    fun execute(loanId: Long): Flow<List<Emi>> {
        return emiRepository.getEmisForLoan(loanId).map { emiEntities ->
            emiEntities.map { emiEntity ->
                Emi(
                    id = emiEntity.id,
                    loanId = emiEntity.loanId,
                    emiNumber = emiEntity.emiNumber,
                    emiAmount = emiEntity.emiAmount,
                    emiDate = emiEntity.emiDate
                )
            }
        }.flowOn(Dispatchers.Default)
    }
}

/**
 * SIMPLER APPROACH:
 * - Load loans first
 * - When user wants to see EMIs, load them separately
 * - No complex nested Flow handling
 */