package com.example.integralcalculator.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {
    @Query("SELECT * FROM history WHERE userId = :userId ORDER BY timestamp DESC")
    fun getHistory(userId: String): Flow<List<HistoryEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: HistoryEntity)
}