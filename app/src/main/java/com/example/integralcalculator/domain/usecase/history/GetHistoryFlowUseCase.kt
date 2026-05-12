package com.example.integralcalculator.domain.usecase.history

import com.example.integralcalculator.domain.model.CalcRecord
import com.example.integralcalculator.domain.repository.HistoryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetHistoryFlowUseCase @Inject constructor(
    private val repository: HistoryRepository
) {
    operator fun invoke(userId: String): Flow<List<CalcRecord>> {
        if (userId.isBlank()) throw IllegalStateException("User not logged in")
        return repository.getHistory(userId)
    }
}