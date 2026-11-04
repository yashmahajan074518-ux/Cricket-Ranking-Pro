package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = ElectricBlue,
    onPrimary = Color(0xFF04111F),
    primaryContainer = Color(0xFF143453),
    onPrimaryContainer = Color(0xFFEDF5FF),
    secondary = TrophyGold,
    onSecondary = Color(0xFF10151E),
    secondaryContainer = Color(0xFF382D10),
    onSecondaryContainer = GoldLight,
    background = BgMidnight,
    onBackground = TextPrimary,
    surface = CardNavy,
    onSurface = TextPrimary,
    surfaceVariant = Color(0xFF0B1424),
    onSurfaceVariant = TextMuted,
    outline = BorderDark,
    outlineVariant = CardNavyBorder,
    error = NegRed,
    onError = Color.White
)

@Composable
fun CricketRankingProTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = CricketTypography,
        content = content
    )
}
