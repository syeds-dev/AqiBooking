package com.mvl.aqibooking.presentation.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mvl.aqibooking.R
import com.mvl.aqibooking.domain.Booking
import com.mvl.aqibooking.domain.BookingHistory
import com.mvl.aqibooking.domain.Coordinate
import com.mvl.aqibooking.domain.Location
import com.mvl.aqibooking.domain.LocationSlot
import com.mvl.aqibooking.domain.SelectedLocation
import com.mvl.aqibooking.presentation.AppColors
import com.mvl.aqibooking.presentation.AqiBookingTheme
import com.mvl.aqibooking.presentation.Dimens
import com.mvl.aqibooking.presentation.ErrorContent
import com.mvl.aqibooking.presentation.LoadingContent
import com.mvl.aqibooking.presentation.labelRes
import com.mvl.aqibooking.presentation.rememberPriceFormat

@Composable
fun HistoryScreen(viewModel: HistoryViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold { padding ->
        val modifier = Modifier.fillMaxSize().padding(padding)
        when (val current = state) {
            HistoryUiState.Loading -> LoadingContent(modifier)
            is HistoryUiState.Error -> ErrorContent(current.message, onRetry = viewModel::onRetry, modifier = modifier)
            is HistoryUiState.Success -> HistoryContent(current.history, modifier)
        }
    }
}

@Composable
private fun HistoryContent(history: BookingHistory, modifier: Modifier) {
    Column(modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.SpaceL),
            horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceL),
        ) {
            Total(stringResource(R.string.label_total_count), history.totalCount.toString(), Modifier.weight(1f))
            Total(stringResource(R.string.label_total_price), rememberPriceFormat().format(history.totalPrice), Modifier.weight(1f))
        }
        HorizontalDivider(thickness = Dimens.SectionDivider, color = AppColors.Divider)

        if (history.bookings.isEmpty()) {
            Text(
                text = stringResource(R.string.history_empty),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Dimens.SpaceL),
                textAlign = TextAlign.Center,
                color = AppColors.TextSecondary,
            )
        }
        LazyColumn {
            items(history.bookings) { booking ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Dimens.SpaceL),
                    verticalArrangement = Arrangement.spacedBy(Dimens.SpaceL),
                ) {
                    HistoryRow(LocationSlot.A, booking.a)
                    HistoryRow(LocationSlot.B, booking.b)
                }
                HorizontalDivider(thickness = Dimens.BorderWidth, color = AppColors.Divider)
            }
        }
    }
}

@Composable
private fun Total(label: String, value: String, modifier: Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Dimens.SpaceS),
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge, color = AppColors.TextTertiary)
        Text(value, style = MaterialTheme.typography.titleLarge)
    }
}

@Composable
private fun HistoryRow(slot: LocationSlot, location: SelectedLocation) {
    Row {
        Text(
            text = stringResource(slot.labelRes()),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(Dimens.ListLetterWidth),
        )
        Text(location.displayName, style = MaterialTheme.typography.bodyLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Preview(showBackground = true)
@Composable
private fun HistoryScreenPreview() {
    fun place(name: String, aqi: Int) = SelectedLocation(Location(Coordinate(37.5, 127.0), name, aqi))
    val history = BookingHistory(
        listOf(
            Booking(place("Seocho District, Yangjae 2(i)-dong", 30), place("Jongno District, Sajik-dong", 40), 10_000.0),
            Booking(place("Mapo District, Hapjeong-dong", 50), place("Songpa District, Jamsil 3-dong", 60), 20_000.0),
        ),
    )
    AqiBookingTheme {
        HistoryContent(history, Modifier.fillMaxSize())
    }
}
