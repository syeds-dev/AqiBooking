package com.mvl.aqibooking.presentation.detail

import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.navigation.toRoute
import com.mvl.aqibooking.domain.LocationSlot
import com.mvl.aqibooking.domain.NICKNAME_MAX_LENGTH
import com.mvl.aqibooking.domain.ObserveTripUseCase
import com.mvl.aqibooking.domain.SelectedLocation
import com.mvl.aqibooking.domain.UpdateNicknameUseCase
import com.mvl.aqibooking.presentation.DetailRoute
import com.mvl.aqibooking.presentation.toMessageRes
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class LocationDetailUiState(
    val slot: LocationSlot,
    val location: SelectedLocation?,
    val nickname: String,
    @StringRes val errorMessage: Int? = null,
)

@HiltViewModel
class LocationDetailViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    observeTrip: ObserveTripUseCase,
    private val updateNickname: UpdateNicknameUseCase,
) : ViewModel() {

    private val slot = LocationSlot.valueOf(savedStateHandle.toRoute<DetailRoute>().slot)
    private val location = observeTrip().value[slot]

    private val _uiState = MutableStateFlow(
        LocationDetailUiState(
            slot = slot,
            location = location,
            nickname = savedStateHandle.get<String>(KEY_NICKNAME) ?: location?.nickname.orEmpty(),
        ),
    )
    val uiState: StateFlow<LocationDetailUiState> = _uiState.asStateFlow()

    fun onNicknameChange(value: String) {
        val limited = value.take(NICKNAME_MAX_LENGTH)
        savedStateHandle[KEY_NICKNAME] = limited
        _uiState.update { it.copy(nickname = limited, errorMessage = null) }
    }

    fun onSave(): Boolean {
        val result = updateNickname(slot, _uiState.value.nickname)
        result.onFailure { error -> _uiState.update { it.copy(errorMessage = error.toMessageRes()) } }
        return result.isSuccess
    }

    private companion object {
        const val KEY_NICKNAME = "nickname"
    }
}
