package com.kimfamily.ledger.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val DarkColorScheme = darkColorScheme(
    primary = LedgerDarkPrimary,
    onPrimary = DarkBackground,
    primaryContainer = DarkSurfaceVariant,
    onPrimaryContainer = LedgerDarkOnBackground,
    secondary = LedgerDarkSecondary,
    background = DarkBackground,
    onBackground = LedgerDarkOnBackground,
    surface = DarkSurface,
    onSurface = LedgerDarkOnBackground,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = LedgerDarkOnBackground.copy(alpha = 0.75f),
    outlineVariant = DarkSurfaceVariant,
)

private val LightColorScheme = lightColorScheme(
    primary = LedgerLightPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFF1DFD7),
    onPrimaryContainer = LedgerLightSecondary,
    secondary = LedgerLightSecondary,
    background = Color(0xFFFBFAF8),
    onBackground = Color(0xFF1C1410),
    surface = Color.White,
    onSurface = Color(0xFF1C1410),
    surfaceVariant = Color(0xFFF0ECE8),
    onSurfaceVariant = Color(0xFF5D4E44),
    outlineVariant = Color(0xFFE2D8D0),
)

@Composable
fun KyungahLedgerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes(
            extraLarge = RoundedCornerShape(20.dp),
            large = RoundedCornerShape(16.dp),
            medium = RoundedCornerShape(12.dp),
        ),
        content = content,
    )
}
