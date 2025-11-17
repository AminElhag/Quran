package com.quran.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

private val LightColors = lightColorScheme(
    primary = Color(0xFF1B5E20),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFA5D6A7),
    onPrimaryContainer = Color(0xFF0D3E10),
    secondary = Color(0xFF4E342E),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD7CCC8),
    onSecondaryContainer = Color(0xFF2E1B15),
    tertiary = Color(0xFF8B7355),
    onTertiary = Color.White,
    background = Color(0xFFF5F0E6),
    onBackground = Color(0xFF2D2416),
    surface = Color(0xFFFAF7F0),
    onSurface = Color(0xFF2D2416),
    surfaceVariant = Color(0xFFEDE8DC),
    onSurfaceVariant = Color(0xFF49454F)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF81C784),
    onPrimary = Color(0xFF0D3E10),
    primaryContainer = Color(0xFF1B5E20),
    onPrimaryContainer = Color(0xFFA5D6A7),
    secondary = Color(0xFFBCAAA4),
    onSecondary = Color(0xFF2E1B15),
    secondaryContainer = Color(0xFF4E342E),
    onSecondaryContainer = Color(0xFFD7CCC8),
    tertiary = Color(0xFFCDDC39),
    onTertiary = Color(0xFF2E3300),
    background = Color(0xFF1C1B1F),
    onBackground = Color(0xFFE6E1E5),
    surface = Color(0xFF1C1B1F),
    onSurface = Color(0xFFE6E1E5),
    surfaceVariant = Color(0xFF49454F),
    onSurfaceVariant = Color(0xFFCAC4D0)
)

@Composable
fun QuranTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors

    CompositionLocalProvider(
        LocalLayoutDirection provides LayoutDirection.Rtl
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = QuranTypography,
            content = content
        )
    }
}
