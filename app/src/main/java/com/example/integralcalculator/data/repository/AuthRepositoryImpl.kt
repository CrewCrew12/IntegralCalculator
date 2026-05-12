package com.example.integralcalculator.data.repository

import com.example.integralcalculator.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth
) : AuthRepository {
    override suspend fun login(email: String, password: String): Result<String> = try {
        val result = auth.signInWithEmailAndPassword(email, password).await()
        Result.success(result.user?.uid ?: "")
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun register(email: String, password: String): Result<String> = try {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        Result.success(result.user?.uid ?: "")
    } catch (e: Exception) {
        Result.failure(e)
    }

    override fun getCurrentUserId(): String? = auth.currentUser?.uid
    override fun logout() = auth.signOut()
}