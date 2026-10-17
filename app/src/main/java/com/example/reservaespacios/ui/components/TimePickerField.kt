@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.reservaespacios.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.reservaespacios.util.DateTimeFormats
import java.time.LocalTime

/**
 * Campo de hora con reloj. El estado sigue siendo un texto "HH:mm" (24 horas).
 * [defaultTime]: hora con la que abre el reloj cuando el campo todavía está vacío.
 */
@Composable
fun TimePickerField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    error: String?,
    isValid: Boolean,
    onBlur: () -> Unit,
    modifier: Modifier = Modifier,
    defaultTime: LocalTime = LocalTime.of(9, 0)
) {
    var showDialog by remember { mutableStateOf(false) }

    PickerField(
        value = value,
        label = label,
        placeholder = "Elegir hora",
        error = error,
        isValid = isValid,
        leadingIcon = Icons.Filled.Schedule,
        onClick = { showDialog = true },
        modifier = modifier
    )

    if (showDialog) {
        val initial = DateTimeFormats.parseTime(value) ?: defaultTime
        val state = rememberTimePickerState(
            initialHour = initial.hour,
            initialMinute = initial.minute,
            is24Hour = true
        )
        AlertDialog(
            onDismissRequest = {
                showDialog = false
                onBlur()
            },
            title = { Text(label) },
            text = {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    TimePicker(state = state)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    onValueChange(DateTimeFormats.formatTime(LocalTime.of(state.hour, state.minute)))
                    showDialog = false
                    onBlur()
                }) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDialog = false
                    onBlur()
                }) { Text("Cancelar") }
            }
        )
    }
}