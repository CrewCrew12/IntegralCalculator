package com.example.integralcalculator.domain.repository

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<String>
    suspend fun register(email: String, password: String): Result<String>
    fun getCurrentUserId(): String?
    fun logout()
}