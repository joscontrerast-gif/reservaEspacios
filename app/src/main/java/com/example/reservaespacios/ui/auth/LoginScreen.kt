package com.example.reservaespacios.ui.auth

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.reservaespacios.R
import com.example.reservaespacios.ui.components.AuthButton
import com.example.reservaespacios.ui.components.AuthLayout
import com.example.reservaespacios.ui.components.FormErrorMessage
import com.example.reservaespacios.ui.components.PasswordTextField
import com.example.reservaespacios.ui.components.ValidatedTextField

@Composable
fun LoginRoute(
    onNavigateToRegister: () -> Unit,
    viewModel: LoginViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LoginScreen(state = state, onEvent = viewModel::onEvent, onNavigateToRegister = onNavigateToRegister)
}

@Composable
fun LoginScreen(
    state: LoginUiState,
    onEvent: (LoginEvent) -> Unit,
    onNavigateToRegister: () -> Unit,
    modifier: Modifier = Modifier
) {
    AuthLayout(subtitle = "RESERVA DE SALAS") {
        Text(
            "Bienvenido/a",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = colorResource(R.color.therion_blue)
        )
        Text(
            "Revisa qué salas están libres y reserva tu próximo bloque",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(12.dp))

        ValidatedTextField(
            value = state.email,
            onValueChange = { onEvent(LoginEvent.EmailChanged(it)) },
            label = "Correo",
            error = state.errorOf(LoginField.EMAIL),
            isValid = state.isFieldValid(LoginField.EMAIL),
            onBlur = { onEvent(LoginEvent.FieldBlurred(LoginField.EMAIL)) },
            leadingIcon = Icons.Filled.Email,
            keyboardType = KeyboardType.Email,
            modifier = Modifier.fillMaxWidth()
        )

        PasswordTextField(
            value = state.password,
            onValueChange = { onEvent(LoginEvent.PasswordChanged(it)) },
            label = "Contraseña",
            error = state.errorOf(LoginField.PASSWORD),
            isValid = state.isFieldValid(LoginField.PASSWORD),
            visible = state.passwordVisible,
            onToggleVisibility = { onEvent(LoginEvent.TogglePasswordVisibility) },
            onBlur = { onEvent(LoginEvent.FieldBlurred(LoginField.PASSWORD)) },
            imeAction = ImeAction.Done,
            modifier = Modifier.fillMaxWidth()
        )

        FormErrorMessage(state.submitError)

        AuthButton(
            text = "Iniciar sesión",
            onClick = { onEvent(LoginEvent.Submit) },
            isLoading = state.isLoading
        )

        TextButton(
            onClick = onNavigateToRegister,
            colors = ButtonDefaults.textButtonColors(contentColor = colorResource(R.color.therion_teal)),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("¿Primera vez aquí? Crea tu cuenta", fontWeight = FontWeight.SemiBold)
        }
    }
}