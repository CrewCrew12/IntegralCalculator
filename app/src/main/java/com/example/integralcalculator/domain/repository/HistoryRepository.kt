package com.example.integralcalculator.domain.repository

import com.example.integralcalculator.domain.model.CalcRecord
import kotlinx.coroutines.flow.Flow

interface HistoryRepository {
    fun getHistory(userId: String): Flow<List<CalcRecord>>
    suspend fun saveRecord(userId: String, record: CalcRecord)
}