package com.mvl.aqibooking.presentation.map

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState
import com.mvl.aqibooking.R
import com.mvl.aqibooking.domain.Coordinate
import com.mvl.aqibooking.domain.Location
import com.mvl.aqibooking.domain.LocationSlot
import com.mvl.aqibooking.domain.SelectedLocation
import com.mvl.aqibooking.domain.TripSelection
import com.mvl.aqibooking.presentation.AppColors
import com.mvl.aqibooking.presentation.AqiBookingTheme
import com.mvl.aqibooking.presentation.Dimens
import com.mvl.aqibooking.presentation.PrimaryButton
import com.mvl.aqibooking.presentation.labelRes
import kotlinx.coroutines.flow.filter

private val DEFAULT_POSITION = LatLng(37.5665, 126.9780)
private const val ZOOM = 16f

private val LOCATION_PERMISSIONS = arrayOf(
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.ACCESS_COARSE_LOCATION,
)

@Composable
fun MapScreen(
    onOpenDetail: (LocationSlot) -> Unit,
    onBook: () -> Unit,
    viewModel: MapViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val cameraState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(DEFAULT_POSITION, ZOOM)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { result -> viewModel.onLocationPermissionResult(result.values.any { it }) }
    LaunchedEffect(Unit) {
        if (!state.isPermissionRequested) permissionLauncher.launch(LOCATION_PERMISSIONS)
    }

    LaunchedEffect(state.moveCameraTo) {
        val target = state.moveCameraTo ?: return@LaunchedEffect
        cameraState.position = CameraPosition.fromLatLngZoom(LatLng(target.latitude, target.longitude), ZOOM)
        viewModel.onCameraMoved()
    }

    LaunchedEffect(cameraState) {
        snapshotFlow { cameraState.isMoving to cameraState.position.target }
            .filter { (isMoving, _) -> !isMoving }
            .collect { (_, target) -> viewModel.onCameraIdle(Coordinate(target.latitude, target.longitude)) }
    }

    state.errorMessage?.let { messageRes ->
        val message = stringResource(messageRes)
        LaunchedEffect(messageRes) {
            snackbarHostState.showSnackbar(message)
            viewModel.onErrorShown()
        }
    }

    MapContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onSlotClick = onOpenDetail,
        onPrimaryClick = {
            if (state.selection.nextSlot == null)
                onBook()
            else
                viewModel.onSetLocationClick()
        },
        map = {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraState,
                properties = MapProperties(isMyLocationEnabled = state.hasLocationPermission),
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = false,
                    mapToolbarEnabled = false,
                    myLocationButtonEnabled = state.hasLocationPermission,
                ),
            )
        },
    )
}

@Composable
private fun MapContent(
    state: MapUiState,
    snackbarHostState: SnackbarHostState,
    onSlotClick: (LocationSlot) -> Unit,
    onPrimaryClick: () -> Unit,
    map: @Composable () -> Unit,
) {
    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        Column(Modifier
            .fillMaxSize()
            .padding(padding)) {
            AqiBar(aqi = state.aqi, isLoading = state.isAqiLoading, isError = state.isAqiError)

            Box(Modifier
                .weight(1f)
                .fillMaxWidth()) {
                map()
                Image(
                    painter = painterResource(R.drawable.ic_center_pin),
                    contentDescription = stringResource(R.string.cd_center_pin),
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset(y = -Dimens.PinHeight / 2)
                        .size(Dimens.PinWidth, Dimens.PinHeight),
                )
            }

            val nextSlot = state.selection.nextSlot
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
                    .padding(Dimens.SpaceL),
                horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceL),
            ) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Dimens.SpaceS)) {
                    LocationSlot.entries.forEach { slot ->
                        SlotLabel(
                            letter = stringResource(slot.labelRes()),
                            name = state.selection[slot]?.displayName,
                            onClick = { onSlotClick(slot) },
                        )
                    }
                }
                PrimaryButton(
                    text = stringResource(
                        when (nextSlot) {
                            LocationSlot.A -> R.string.action_set_a
                            LocationSlot.B -> R.string.action_set_b
                            null -> R.string.action_book
                        },
                    ),
                    onClick = onPrimaryClick,
                    isLoading = state.isSettingLocation,
                    modifier = Modifier
                        .width(Dimens.ActionButtonWidth)
                        .fillMaxHeight(),
                )
            }
        }
    }
}

@Composable
private fun AqiBar(aqi: Int?, isLoading: Boolean, isError: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(Dimens.TopBarHeight)
            .padding(horizontal = Dimens.SpaceL),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(stringResource(R.string.label_aqi), style = MaterialTheme.typography.titleSmall, color = AppColors.TextSecondary)
        Spacer(Modifier.width(Dimens.SpaceL))
        when {
            isLoading -> CircularProgressIndicator(Modifier.size(Dimens.SmallProgress), color = AppColors.TextPrimary)
            isError -> Text(stringResource(R.string.aqi_unavailable), style = MaterialTheme.typography.titleSmall, color = AppColors.Error)
            else -> Text(aqi?.toString() ?: stringResource(R.string.aqi_placeholder), style = MaterialTheme.typography.titleSmall)
        }
    }
}

@Composable
private fun SlotLabel(letter: String, name: String?, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(Dimens.LabelHeight)
            .clip(RoundedCornerShape(Dimens.CornerRadius))
            .background(AppColors.LabelBackground)
            .clickable(enabled = name != null, onClick = onClick)
            .padding(horizontal = Dimens.SpaceL),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceS),
    ) {
        Text(letter, style = MaterialTheme.typography.titleMedium, modifier = Modifier.width(Dimens.SlotLetterWidth))
        Text(name.orEmpty(), style = MaterialTheme.typography.bodyLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Preview(showBackground = true)
@Composable
private fun MapScreenPreview() {
    val seocho = Location(Coordinate(37.484, 127.034), "Seocho District, Yangjae 2(i)-dong", 42)
    AqiBookingTheme {
        MapContent(
            state = MapUiState(selection = TripSelection(a = SelectedLocation(seocho)), aqi = 80),
            snackbarHostState = remember { SnackbarHostState() },
            onSlotClick = {},
            onPrimaryClick = {},
            map = { Box(Modifier
                .fillMaxSize()
                .background(AppColors.LabelBackground)) },
        )
    }
}
