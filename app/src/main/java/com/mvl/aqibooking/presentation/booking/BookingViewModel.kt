package com.mvl.aqibooking.presentation.booking

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mvl.aqibooking.domain.BookTripUseCase
import com.mvl.aqibooking.domain.Booking
import com.mvl.aqibooking.presentation.toMessageRes
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface BookingUiState {
    data object Loading : BookingUiState
    data class Success(val booking: Booking) : BookingUiState
    data class Error(@StringRes val message: Int) : BookingUiState
}

@HiltViewModel
class BookingViewModel @Inject constructor(
    private val bookTrip: BookTripUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<BookingUiState>(BookingUiState.Loading)
    val uiState: StateFlow<BookingUiState> = _uiState.asStateFlow()

    init {
        book()
    }

    fun onRetry() = book()

    private fun book() {
        _uiState.value = BookingUiState.Loading
        viewModelScope.launch {
            _uiState.value = bookTrip().fold(
                onSuccess = { BookingUiState.Success(it) },
                onFailure = { BookingUiState.Error(it.toMessageRes()) },
            )
        }
    }
}
