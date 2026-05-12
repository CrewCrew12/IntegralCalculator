package com.example.integralcalculator.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "calculations")
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val expression: String,
    val variable: String,
    val resultLatex: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isDefinite: Boolean
)