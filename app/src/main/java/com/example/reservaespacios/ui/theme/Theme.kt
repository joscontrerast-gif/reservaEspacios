package com.example.reservaespacios.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.LocalAutofillHighlightColor

private val Azul = Color(0xFF013A58)
private val Turquesa = Color(0xFF028E98)

private val ColoresMarca = lightColorScheme(
    primary = Azul,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD3E6F2),
    onPrimaryContainer = Azul,
    secondary = Turquesa,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD0EEF0),
    onSecondaryContainer = Color(0xFF014F55),
    background = Color.White,
    surface = Color.White,
    surfaceVariant = Color(0xFFE6EDF1),
    onSurfaceVariant = Color(0xFF42525C),
    outline = Color(0xFF73838D),
    outlineVariant = Color(0xFFC5D0D6),
    surfaceBright = Color.White,
    surfaceDim = Color(0xFFDDE5EB),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFF6F9FB),
    surfaceContainer = Color(0xFFF0F5F9),
    surfaceContainerHigh = Color(0xFFEAF0F5),
    surfaceContainerHighest = Color(0xFFE4EBF1)
)

@Composable
fun ReservaEspaciosTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ColoresMarca,
        shapes = Shapes(
            extraSmall = RoundedCornerShape(14.dp),
            small = RoundedCornerShape(16.dp),
            medium = RoundedCornerShape(20.dp)
        )
    ) {
        CompositionLocalProvider(LocalAutofillHighlightColor provides Color.Transparent) {
            content()
        }
    }
}