package com.chessacademy.core.designsystem.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = ChessPrimaryLight,
    onPrimary = ChessOnPrimaryLight,
    primaryContainer = ChessPrimaryContainerLight,
    onPrimaryContainer = ChessOnPrimaryContainerLight,
    secondary = ChessSecondaryLight,
    onSecondary = ChessOnSecondaryLight,
    secondaryContainer = ChessSecondaryContainerLight,
    onSecondaryContainer = ChessOnSecondaryContainerLight,
    background = ChessBackgroundLight,
    onBackground = ChessOnBackgroundLight,
    surface = ChessSurfaceLight,
    onSurface = ChessOnSurfaceLight
)

private val DarkColorScheme = darkColorScheme(
    primary = ChessPrimaryDark,
    onPrimary = ChessOnPrimaryDark,
    primaryContainer = ChessPrimaryContainerDark,
    onPrimaryContainer = ChessOnPrimaryContainerDark,
    secondary = ChessSecondaryDark,
    onSecondary = ChessOnSecondaryDark,
    secondaryContainer = ChessSecondaryContainerDark,
    onSecondaryContainer = ChessOnSecondaryContainerDark,
    background = ChessBackgroundDark,
    onBackground = ChessOnBackgroundDark,
    surface = ChessSurfaceDark,
    onSurface = ChessOnSurfaceDark
)

/**
 * Haupt-Theme für die Chess Academy Anwendung.
 * Unterstützt dynamische Android-Farben (Material You ab API 31) mit automatischem Fallback
 * auf die maßgeschneiderte Schach-Farbpalette für API 26-30.
 */
@Composable
fun ChessAcademyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = ChessAcademyTypography,
        content = content
    )
}
