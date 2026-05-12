package com.example.integralcalculator.di

import android.content.Context
import androidx.room.Room
import com.example.integralcalculator.data.datasource.PythonSolverDataSource
import com.example.integralcalculator.data.local.AppDatabase
import com.example.integralcalculator.data.repository.AuthRepositoryImpl
import com.example.integralcalculator.data.repository.HistoryRepositoryImpl
import com.example.integralcalculator.data.repository.IntegralRepositoryImpl
import com.example.integralcalculator.domain.repository.AuthRepository
import com.example.integralcalculator.domain.repository.HistoryRepository
import com.example.integralcalculator.domain.repository.IntegralRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()
    @Provides @Singleton
    fun provideFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()
    @Provides @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(context, AppDatabase::class.java, "calc_db")
            .fallbackToDestructiveMigration()
            .build()
    }
    @Provides @Singleton
    fun provideHistoryDao(db: AppDatabase) = db.historyDao()
    @Provides @Singleton
    fun provideAuthRepository(auth: FirebaseAuth): AuthRepository = AuthRepositoryImpl(auth)
    @Provides @Singleton
    fun provideHistoryRepository(
        dao: AppDatabase,
        firestore: FirebaseFirestore
    ): HistoryRepository {
        return HistoryRepositoryImpl(dao.historyDao(), firestore)
    }
    @Provides @Singleton
    fun provideIntegralRepository(dataSource: PythonSolverDataSource): IntegralRepository {
        return IntegralRepositoryImpl(dataSource)
    }
    @Provides @Singleton
    fun providePythonDataSource(@ApplicationContext context: Context): PythonSolverDataSource {
        return PythonSolverDataSource(context)
    }
}