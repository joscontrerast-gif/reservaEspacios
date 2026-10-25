package com.example.reservaespacios.ui.auth

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
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
import com.example.reservaespacios.model.UserProfile
import com.example.reservaespacios.ui.components.AuthButton
import com.example.reservaespacios.ui.components.AuthLayout
import com.example.reservaespacios.ui.components.FormErrorMessage
import com.example.reservaespacios.ui.components.PasswordTextField
import com.example.reservaespacios.ui.components.ValidatedDropdown
import com.example.reservaespacios.ui.components.ValidatedTextField

@Composable
fun RegisterRoute(
    onNavigateToLogin: () -> Unit,
    viewModel: RegisterViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    RegisterScreen(state = state, onEvent = viewModel::onEvent, onNavigateToLogin = onNavigateToLogin)
}

@Composable
fun RegisterScreen(
    state: RegisterUiState,
    onEvent: (RegisterEvent) -> Unit,
    onNavigateToLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    AuthLayout(subtitle = "RESERVA DE SALAS") {
        Text(
            "Crea tu cuenta",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = colorResource(R.color.therion_blue)
        )
        Text(
            "Solo toma un minuto. Después podrás reservar salas y ver tu agenda.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))

        ValidatedTextField(
            value = state.name,
            onValueChange = { onEvent(RegisterEvent.NameChanged(it)) },
            label = "Nombre y apellido",
            error = state.errorOf(RegisterField.NAME),
            isValid = state.isFieldValid(RegisterField.NAME),
            onBlur = { onEvent(RegisterEvent.FieldBlurred(RegisterField.NAME)) },
            leadingIcon = Icons.Filled.Person,
            modifier = Modifier.fillMaxWidth()
        )

        ValidatedTextField(
            value = state.email,
            onValueChange = { onEvent(RegisterEvent.EmailChanged(it)) },
            label = "Correo electrónico",
            error = state.errorOf(RegisterField.EMAIL),
            isValid = state.isFieldValid(RegisterField.EMAIL),
            onBlur = { onEvent(RegisterEvent.FieldBlurred(RegisterField.EMAIL)) },
            leadingIcon = Icons.Filled.Email,
            keyboardType = KeyboardType.Email,
            modifier = Modifier.fillMaxWidth()
        )

        ValidatedDropdown(
            label = "Perfil",
            options = UserProfile.selfRegistrable,
            selected = state.profile,
            optionLabel = { it.label },
            error = state.errorOf(RegisterField.PROFILE),
            onSelected = { onEvent(RegisterEvent.ProfileSelected(it)) },
            onDismiss = { onEvent(RegisterEvent.FieldBlurred(RegisterField.PROFILE)) },
            leadingIcon = Icons.Filled.Badge
        )

        PasswordTextField(
            value = state.password,
            onValueChange = { onEvent(RegisterEvent.PasswordChanged(it)) },
            label = "Contraseña",
            error = state.errorOf(RegisterField.PASSWORD),
            isValid = state.isFieldValid(RegisterField.PASSWORD),
            visible = state.passwordVisible,
            onToggleVisibility = { onEvent(RegisterEvent.TogglePasswordVisibility) },
            onBlur = { onEvent(RegisterEvent.FieldBlurred(RegisterField.PASSWORD)) },
            modifier = Modifier.fillMaxWidth()
        )

        PasswordTextField(
            value = state.confirmPassword,
            onValueChange = { onEvent(RegisterEvent.ConfirmPasswordChanged(it)) },
            label = "Confirma tu contraseña",
            error = state.errorOf(RegisterField.CONFIRM_PASSWORD),
            isValid = state.isFieldValid(RegisterField.CONFIRM_PASSWORD),
            visible = state.passwordVisible,
            onToggleVisibility = { onEvent(RegisterEvent.TogglePasswordVisibility) },
            onBlur = { onEvent(RegisterEvent.FieldBlurred(RegisterField.CONFIRM_PASSWORD)) },
            imeAction = ImeAction.Done,
            modifier = Modifier.fillMaxWidth()
        )

        FormErrorMessage(state.submitError)

        AuthButton(
            text = "Crear mi cuenta",
            onClick = { onEvent(RegisterEvent.Submit) },
            isLoading = state.isLoading
        )

        TextButton(
            onClick = onNavigateToLogin,
            colors = ButtonDefaults.textButtonColors(contentColor = colorResource(R.color.therion_teal)),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("¿Ya tienes cuenta? Inicia sesión", fontWeight = FontWeight.SemiBold)
        }
    }
}