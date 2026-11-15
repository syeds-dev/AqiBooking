package com.mvl.aqibooking.presentation.booking

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
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
import com.mvl.aqibooking.domain.Booking
import com.mvl.aqibooking.domain.Coordinate
import com.mvl.aqibooking.domain.Location
import com.mvl.aqibooking.domain.LocationSlot
import com.mvl.aqibooking.domain.SelectedLocation
import com.mvl.aqibooking.presentation.AppColors
import com.mvl.aqibooking.presentation.AqiBookingTheme
import com.mvl.aqibooking.presentation.Dimens
import com.mvl.aqibooking.presentation.ErrorContent
import com.mvl.aqibooking.presentation.InfoRow
import com.mvl.aqibooking.presentation.LoadingContent
import com.mvl.aqibooking.presentation.PrimaryButton
import com.mvl.aqibooking.presentation.LocationSection
import com.mvl.aqibooking.presentation.rememberPriceFormat

@Composable
fun BookingScreen(
    onOpenHistory: () -> Unit,
    viewModel: BookingViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold { padding ->
        val modifier = Modifier.fillMaxSize().padding(padding)
        when (val current = state) {
            BookingUiState.Loading -> LoadingContent(modifier)
            is BookingUiState.Error -> ErrorContent(current.message, onRetry = viewModel::onRetry, modifier = modifier)
            is BookingUiState.Success -> BookingContent(current.booking, onOpenHistory, modifier)
        }
    }
}

@Composable
private fun BookingContent(booking: Booking, onOpenHistory: () -> Unit, modifier: Modifier) {
    Column(modifier) {
        BookedLocation(LocationSlot.A, booking.a)
        HorizontalDivider(thickness = Dimens.BorderWidth, color = AppColors.Divider)
        BookedLocation(LocationSlot.B, booking.b)

        Spacer(Modifier.weight(1f))

        Column(
            modifier = Modifier.padding(Dimens.SpaceL),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpaceL),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(stringResource(R.string.label_price), style = MaterialTheme.typography.titleLarge, color = AppColors.TextSecondary)
                Text(rememberPriceFormat().format(booking.price), style = MaterialTheme.typography.titleLarge)
            }
            PrimaryButton(
                text = stringResource(R.string.action_view_history),
                onClick = onOpenHistory,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimens.ButtonHeight),
            )
        }
    }
}

@Composable
private fun BookedLocation(slot: LocationSlot, selected: SelectedLocation) {
    LocationSection(
        slot = slot,
        title = selected.location.name,
        modifier = Modifier
            .fillMaxWidth()
            .padding(Dimens.SpaceL),
    ) {
        InfoRow(stringResource(R.string.label_aqi), selected.location.aqi.toString())
        val nickname = selected.nickname
        if (nickname != null) {
            InfoRow(stringResource(R.string.label_nickname), nickname)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun BookingScreenPreview() {
    val a = Location(Coordinate(37.484, 127.034), "Seocho District, Yangjae 2(i)-dong", 30)
    val b = Location(Coordinate(37.573, 126.979), "Jongno District, Sajik-dong", 40)
    AqiBookingTheme {
        BookingContent(
            booking = Booking(SelectedLocation(a, "home"), SelectedLocation(b, "office"), price = 14_200.0),
            onOpenHistory = {},
            modifier = Modifier.fillMaxSize(),
        )
    }
}
