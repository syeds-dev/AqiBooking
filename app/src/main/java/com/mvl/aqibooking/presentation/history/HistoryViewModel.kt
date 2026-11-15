package com.mvl.aqibooking.presentation.history

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mvl.aqibooking.domain.BookingHistory
import com.mvl.aqibooking.domain.GetMonthlyHistoryUseCase
import com.mvl.aqibooking.presentation.toMessageRes
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface HistoryUiState {
    data object Loading : HistoryUiState
    data class Success(val history: BookingHistory) : HistoryUiState
    data class Error(@StringRes val message: Int) : HistoryUiState
}

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val getMonthlyHistory: GetMonthlyHistoryUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<HistoryUiState>(HistoryUiState.Loading)
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun onRetry() = load()

    private fun load() {
        _uiState.value = HistoryUiState.Loading
        viewModelScope.launch {
            _uiState.value = getMonthlyHistory().fold(
                onSuccess = { HistoryUiState.Success(it) },
                onFailure = { HistoryUiState.Error(it.toMessageRes()) },
            )
        }
    }
}
