package com.mvl.aqibooking.presentation.map

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mvl.aqibooking.domain.Coordinate
import com.mvl.aqibooking.domain.GetAirQualityUseCase
import com.mvl.aqibooking.domain.GetCurrentLocationUseCase
import com.mvl.aqibooking.domain.ObserveTripUseCase
import com.mvl.aqibooking.domain.SelectLocationUseCase
import com.mvl.aqibooking.domain.TripSelection
import com.mvl.aqibooking.presentation.toMessageRes
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MapUiState(
    val selection: TripSelection = TripSelection(),
    val aqi: Int? = null,
    val isAqiLoading: Boolean = false,
    val isAqiError: Boolean = false,
    val isSettingLocation: Boolean = false,
    val isPermissionRequested: Boolean = false,
    val hasLocationPermission: Boolean = false,
    val moveCameraTo: Coordinate? = null,
    @StringRes val errorMessage: Int? = null,
)

@HiltViewModel
class MapViewModel @Inject constructor(
    observeTrip: ObserveTripUseCase,
    private val getAirQuality: GetAirQualityUseCase,
    private val getCurrentLocation: GetCurrentLocationUseCase,
    private val selectLocation: SelectLocationUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    private val markerPosition = MutableStateFlow<Coordinate?>(null)

    init {
        viewModelScope.launch {
            observeTrip().collect {
                selection -> _uiState.update {
                    it.copy(selection = selection)
                }
            }
        }
        observeAirQuality()
    }

    @OptIn(FlowPreview::class)
    private fun observeAirQuality() {
        viewModelScope.launch {
            markerPosition
                .filterNotNull()
                .debounce(AQI_DEBOUNCE_MILLIS)
                .distinctUntilChanged()
                .collectLatest { position ->
                    _uiState.update { it.copy(isAqiLoading = true, isAqiError = false) }
                    val result = getAirQuality(position)
                    _uiState.update {
                        it.copy(isAqiLoading = false, aqi = result.getOrNull(), isAqiError = result.isFailure)
                    }
                }
        }
    }

    fun onLocationPermissionResult(granted: Boolean) {
        _uiState.update { it.copy(isPermissionRequested = true, hasLocationPermission = granted) }
        if (granted) {
            viewModelScope.launch {
                val location = getCurrentLocation()
                _uiState.update { it.copy(moveCameraTo = location) }
            }
        }
    }

    fun onCameraMoved() = _uiState.update {
        it.copy(moveCameraTo = null)
    }

    fun onCameraIdle(position: Coordinate) {
        markerPosition.value = position
    }

    fun onSetLocationClick() {
        val position = markerPosition.value ?: return
        if (_uiState.value.isSettingLocation) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSettingLocation = true) }
            val result = selectLocation(position)
            _uiState.update {
                it.copy(isSettingLocation = false, errorMessage = result.exceptionOrNull()?.toMessageRes())
            }
        }
    }

    fun onErrorShown() = _uiState.update { it.copy(errorMessage = null) }

    private companion object {
        const val AQI_DEBOUNCE_MILLIS = 300L
    }
}
