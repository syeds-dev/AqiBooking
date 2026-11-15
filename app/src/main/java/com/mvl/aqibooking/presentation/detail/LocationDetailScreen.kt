package com.mvl.aqibooking.presentation.detail

import androidx.compose.foundation.layout.Column
import com.mvl.aqibooking.presentation.LocationSection
import com.mvl.aqibooking.presentation.AppColors
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mvl.aqibooking.R
import com.mvl.aqibooking.domain.Coordinate
import com.mvl.aqibooking.domain.Location
import com.mvl.aqibooking.domain.LocationSlot
import com.mvl.aqibooking.domain.NICKNAME_MAX_LENGTH
import com.mvl.aqibooking.domain.SelectedLocation
import com.mvl.aqibooking.presentation.AqiBookingTheme
import com.mvl.aqibooking.presentation.Dimens
import com.mvl.aqibooking.presentation.InfoRow
import com.mvl.aqibooking.presentation.PrimaryButton

@Composable
fun LocationDetailScreen(
    onDone: () -> Unit,
    viewModel: LocationDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LocationDetailContent(
        state = state,
        onNicknameChange = viewModel::onNicknameChange,
        onSave = { if (viewModel.onSave()) onDone() },
    )
}

@Composable
private fun LocationDetailContent(
    state: LocationDetailUiState,
    onNicknameChange: (String) -> Unit,
    onSave: () -> Unit,
) {
    val selected = state.location ?: return

    Scaffold(contentWindowInsets = WindowInsets.safeDrawing) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            LocationSection(
                slot = state.slot,
                title = selected.location.name,
                modifier = Modifier.padding(Dimens.SpaceL),
            ) {
                InfoRow(stringResource(R.string.label_aqi), selected.location.aqi.toString())
            }

            Spacer(Modifier.weight(1f))

            Column(
                modifier = Modifier.padding(Dimens.SpaceL),
                verticalArrangement = Arrangement.spacedBy(Dimens.SpaceL),
            ) {
                OutlinedTextField(
                    value = state.nickname,
                    onValueChange = onNicknameChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = Dimens.ButtonHeight),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.titleMedium,
                    placeholder = { Text(stringResource(R.string.label_nickname), style = MaterialTheme.typography.titleMedium) },
                    isError = state.errorMessage != null,
                    supportingText = {
                        val error = state.errorMessage
                        Text(
                            if (error != null) stringResource(error)
                            else stringResource(R.string.nickname_counter, state.nickname.length, NICKNAME_MAX_LENGTH),
                        )
                    },
                    shape = RoundedCornerShape(Dimens.CornerRadius),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = AppColors.Border,
                        focusedBorderColor = AppColors.TextSecondary,
                        cursorColor = AppColors.TextPrimary,
                    ),
                )
                PrimaryButton(
                    text = stringResource(R.string.action_save),
                    onClick = onSave,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(Dimens.ButtonHeight),
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LocationDetailPreview() {
    val seocho = Location(Coordinate(37.484, 127.034), "Seocho District, Yangjae 2(i)-dong", 42)
    AqiBookingTheme {
        LocationDetailContent(
            state = LocationDetailUiState(slot = LocationSlot.A, location = SelectedLocation(seocho), nickname = "home"),
            onNicknameChange = {},
            onSave = {},
        )
    }
}
