package com.example.integralcalculator.data.repository

import com.example.integralcalculator.data.local.HistoryDao
import com.example.integralcalculator.data.local.HistoryEntity
import com.example.integralcalculator.domain.model.CalcRecord
import com.example.integralcalculator.domain.repository.HistoryRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HistoryRepositoryImpl @Inject constructor(
    private val dao: HistoryDao,
    private val firestore: FirebaseFirestore
) : HistoryRepository {

    override fun getHistory(userId: String): Flow<List<CalcRecord>> =
        dao.getHistory(userId).map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun saveRecord(userId: String, record: CalcRecord) {
        dao.insert(record.toEntity(userId))
        try {
            firestore.collection("users")
                .document(userId)
                .collection("history")
                .add(record.toMap())
                .await()
        } catch (_: Exception) {}
    }
    private fun HistoryEntity.toDomain() = CalcRecord(id, expression, variable, resultLatex, timestamp, isDefinite)
    private fun CalcRecord.toEntity(userId: String) = HistoryEntity(id, userId, expression, variable, result, timestamp, isDefinite)
    private fun CalcRecord.toMap() = mapOf(
        "expression" to expression, "variable" to variable,
        "result" to result, "timestamp" to timestamp, "isDefinite" to isDefinite
    )
}