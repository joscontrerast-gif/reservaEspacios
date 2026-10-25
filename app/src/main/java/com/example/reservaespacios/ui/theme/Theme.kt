package com.example.reservaespacios.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

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
    outlineVariant = Color(0xFFC5D0D6)
)

@Composable
fun ReservaEspaciosTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = ColoresMarca, content = content)
}