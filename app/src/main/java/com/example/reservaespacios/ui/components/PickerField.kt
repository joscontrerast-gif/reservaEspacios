package com.example.reservaespacios.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role

/**
 * Campo de solo lectura que abre un selector al tocarlo (fecha, hora...).
 * Muestra el mismo feedback visual que ValidatedTextField: ícono de error / de éxito y mensaje.
 */
@Composable
fun PickerField(
    value: String,
    label: String,
    placeholder: String,
    error: String?,
    isValid: Boolean,
    leadingIcon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }

    Box(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            placeholder = { Text(placeholder) },
            leadingIcon = { Icon(leadingIcon, contentDescription = null) },
            trailingIcon = {
                when {
                    error != null -> Icon(
                        Icons.Filled.Error,
                        contentDescription = "Campo con error",
                        tint = MaterialTheme.colorScheme.error
                    )
                    isValid -> Icon(
                        Icons.Filled.CheckCircle,
                        contentDescription = "Campo válido",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            },
            isError = error != null,
            supportingText = error?.let { { Text(it) } },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        // Capa transparente: un campo de texto "se come" los toques, así que el clic va encima.
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClickLabel = "Elegir $label",
                    role = Role.Button,
                    onClick = onClick
                )
        )
    }
}