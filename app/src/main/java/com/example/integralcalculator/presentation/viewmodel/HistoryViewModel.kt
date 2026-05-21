package com.example.integralcalculator.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.integralcalculator.domain.model.CalcRecord
import com.example.integralcalculator.domain.usecase.auth.GetCurrentUserUseCase
import com.example.integralcalculator.domain.usecase.history.GetHistoryFlowUseCase
import com.example.integralcalculator.domain.usecase.history.SaveHistoryRecordUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val getHistoryFlowUseCase: GetHistoryFlowUseCase,
    private val saveHistoryRecordUseCase: SaveHistoryRecordUseCase
) : ViewModel() {

    private val _userId = MutableStateFlow<String?>(null)
    val userId: StateFlow<String?> = _userId

    private val _history = MutableStateFlow<List<CalcRecord>>(emptyList())
    val history: StateFlow<List<CalcRecord>> = _history

    init {
        loadUserId()
    }

    private fun loadUserId() {
        val id = getCurrentUserUseCase()
        _userId.value = id
        if (id != null) {
            observeHistory(id)
        }
    }

    private fun observeHistory(userId: String) {
        viewModelScope.launch {
            getHistoryFlowUseCase(userId).collectLatest { records ->
                _history.value = records
            }
        }
    }

    fun saveRecord(record: CalcRecord) {
        viewModelScope.launch {
            val userId = _userId.value ?: return@launch
            saveHistoryRecordUseCase(userId, record)
        }
    }
}