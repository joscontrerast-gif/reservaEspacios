@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.reservaespacios.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.reservaespacios.util.DateTimeFormats
import java.time.LocalDate

/**
 * Campo de fecha con calendario. El estado sigue siendo un texto "dd/MM/yyyy",
 * por lo que el ViewModel y los validadores no cambian.
 * [minDate]: los días anteriores aparecen deshabilitados en el calendario.
 */
@Composable
fun DatePickerField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    error: String?,
    isValid: Boolean,
    onBlur: () -> Unit,
    modifier: Modifier = Modifier,
    minDate: LocalDate? = null
) {
    var showDialog by remember { mutableStateOf(false) }

    PickerField(
        value = value,
        label = label,
        placeholder = "Elegir fecha",
        error = error,
        isValid = isValid,
        leadingIcon = Icons.Filled.DateRange,
        onClick = { showDialog = true },
        modifier = modifier
    )

    if (showDialog) {
        val state = rememberDatePickerState(
            initialSelectedDateMillis = DateTimeFormats.parseDate(value)?.let(DateTimeFormats::dateToPickerMillis),
            selectableDates = remember(minDate) { MinDateSelectable(minDate) }
        )
        DatePickerDialog(
            onDismissRequest = {
                showDialog = false
                onBlur() // al cerrar sin elegir, un campo obligatorio vacío muestra su error
            },
            confirmButton = {
                TextButton(
                    enabled = state.selectedDateMillis != null,
                    onClick = {
                        state.selectedDateMillis?.let { millis ->
                            onValueChange(DateTimeFormats.formatDate(DateTimeFormats.pickerMillisToDate(millis)))
                        }
                        showDialog = false
                        onBlur()
                    }
                ) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDialog = false
                    onBlur()
                }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = state)
        }
    }
}

private class MinDateSelectable(private val minDate: LocalDate?) : SelectableDates {
    override fun isSelectableDate(utcTimeMillis: Long): Boolean {
        if (minDate == null) return true
        return !DateTimeFormats.pickerMillisToDate(utcTimeMillis).isBefore(minDate)
    }
}