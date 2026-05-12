package com.example.integralcalculator.domain.usecase.history

import com.example.integralcalculator.domain.model.CalcRecord
import com.example.integralcalculator.domain.repository.HistoryRepository
import javax.inject.Inject

class SaveHistoryRecordUseCase @Inject constructor(
    private val repository: HistoryRepository
) {
    suspend operator fun invoke(userId: String, record: CalcRecord) {
        if (userId.isBlank()) throw IllegalStateException("User not logged in")

        repository.saveRecord(userId, record)
    }
}