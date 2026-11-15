package com.mvl.aqibooking.presentation

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object AppColors {
    val Yellow = Color(0xFFFFC400)
    val TextPrimary = Color(0xFF000000)
    val TextSecondary = Color(0xFF67696A)
    val TextTertiary = Color(0xFF7C7E80)
    val LabelBackground = Color(0xFFF2F5F7)
    val Divider = Color(0xFFE8EBED)
    val Border = Color(0xFFD9DDE0)
    val Error = Color(0xFFD93025)
}

object Dimens {
    val SpaceS = 8.dp
    val SpaceL = 16.dp
    val SpaceXl = 24.dp
    val TopBarHeight = 60.dp
    val LabelHeight = 64.dp
    val ButtonHeight = 64.dp
    val ActionButtonWidth = 96.dp
    val CornerRadius = 8.dp
    val BorderWidth = 1.dp
    val SlotLetterWidth = 24.dp
    val ListLetterWidth = 40.dp
    val SectionDivider = 8.dp
    val PinWidth = 28.dp
    val PinHeight = 40.dp
    val SmallProgress = 20.dp
}

private val AppTypography = Typography(
    titleLarge = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold, lineHeight = 32.sp),
    titleMedium = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold, lineHeight = 28.sp),
    titleSmall = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold, lineHeight = 24.sp),
    bodyLarge = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal, lineHeight = 20.sp),
)

@Composable
fun AqiBookingTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = AppColors.Yellow,
            onPrimary = AppColors.TextPrimary,
            background = Color.White,
            surface = Color.White,
            onSurface = AppColors.TextPrimary,
            outline = AppColors.Border,
            error = AppColors.Error,
        ),
        typography = AppTypography,
        content = content,
    )
}
