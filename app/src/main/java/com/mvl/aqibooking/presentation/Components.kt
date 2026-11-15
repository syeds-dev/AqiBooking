package com.mvl.aqibooking.presentation

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.mvl.aqibooking.R
import com.mvl.aqibooking.domain.LocationSlot
import java.io.IOException
import java.text.NumberFormat

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
) {
    Button(
        onClick = onClick,
        enabled = !isLoading,
        modifier = modifier,
        shape = RoundedCornerShape(Dimens.CornerRadius),
        contentPadding = PaddingValues(horizontal = Dimens.SpaceL),
        colors = ButtonDefaults.buttonColors(
            containerColor = AppColors.Yellow,
            contentColor = AppColors.TextPrimary,
            disabledContainerColor = AppColors.Yellow.copy(alpha = 0.6f),
            disabledContentColor = AppColors.TextPrimary,
        ),
    ) {
        if (isLoading) {
            CircularProgressIndicator(Modifier.size(Dimens.SmallProgress), color = AppColors.TextPrimary)
        } else {
            Text(text, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center, maxLines = 1, softWrap = false)
        }
    }
}

@Composable
fun LocationSection(
    slot: LocationSlot,
    title: String,
    modifier: Modifier = Modifier,
    details: @Composable ColumnScope.() -> Unit,
) {
    Row(modifier) {
        Text(
            text = stringResource(slot.labelRes()),
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(Dimens.SlotLetterWidth),
        )
        Spacer(Modifier.width(Dimens.SpaceXl))
        Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpaceL)) {
            Text(title, style = MaterialTheme.typography.titleLarge, maxLines = 2, overflow = TextOverflow.Ellipsis)
            details()
        }
    }
}

@Composable
fun InfoRow(label: String, value: String, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = AppColors.TextSecondary,
            modifier = Modifier.weight(1f),
        )
        Text(value, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
    }
}

@Composable
fun LoadingContent(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = AppColors.TextPrimary)
    }
}

@Composable
fun ErrorContent(@StringRes message: Int, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().padding(Dimens.SpaceL),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(stringResource(message), style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
        TextButton(onClick = onRetry) {
            Text(stringResource(R.string.retry), color = AppColors.TextPrimary)
        }
    }
}

@Composable
fun rememberPriceFormat(): NumberFormat = remember {
    NumberFormat.getNumberInstance().apply { maximumFractionDigits = 2 }
}

@StringRes
fun LocationSlot.labelRes(): Int = if (this == LocationSlot.A) R.string.slot_a else R.string.slot_b

@StringRes
fun Throwable.toMessageRes(): Int = if (this is IOException) R.string.error_network else R.string.error_generic

@Preview(showBackground = true)
@Composable
private fun PrimaryButtonPreview() {
    AqiBookingTheme {
        Column(Modifier.padding(Dimens.SpaceL), verticalArrangement = Arrangement.spacedBy(Dimens.SpaceL)) {
            PrimaryButton(text = "Set A", onClick = {}, modifier = Modifier.width(Dimens.ActionButtonWidth))
            PrimaryButton(text = "Set A", onClick = {}, isLoading = true, modifier = Modifier.width(Dimens.ActionButtonWidth))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LocationSectionPreview() {
    AqiBookingTheme {
        LocationSection(
            slot = LocationSlot.A,
            title = "Seocho District, Yangjae 2(i)-dong",
            modifier = Modifier.padding(Dimens.SpaceL),
        ) {
            InfoRow(label = "AQI", value = "30")
            InfoRow(label = "Nickname", value = "home")
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun InfoRowPreview() {
    AqiBookingTheme {
        InfoRow(label = "Price", value = "14,200", modifier = Modifier.padding(Dimens.SpaceL))
    }
}

@Preview(showBackground = true, heightDp = 200)
@Composable
private fun LoadingContentPreview() {
    AqiBookingTheme {
        LoadingContent()
    }
}

@Preview(showBackground = true, heightDp = 200)
@Composable
private fun ErrorContentPreview() {
    AqiBookingTheme {
        ErrorContent(message = R.string.error_network, onRetry = {})
    }
}
