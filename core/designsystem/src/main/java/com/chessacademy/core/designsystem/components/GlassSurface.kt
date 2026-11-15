package com.chessacademy.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Wiederverwendbare GlassSurface mit konfigurierbarer Transparenz, abgerundeten Ecken 
 * und dezenter Randgestaltung. 
 * Kompatibel und sicher auf allen Android-Versionen von API 26 bis 35+ (vermeidet Performance- 
 * und Rendering-Probleme älterer API-Level bei Blur-Effekten).
 */
@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 16.dp,
    transparency: Float = 0.8f,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    borderColor: Color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
    borderWidth: Dp = 1.dp,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(backgroundColor.copy(alpha = transparency.coerceIn(0.0f, 1.0f)))
            .border(borderWidth, borderColor, RoundedCornerShape(cornerRadius))
    ) {
        content()
    }
}
