package com.catedra.bitacora.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun UsernameScreen(
    authState: AuthState,
    usernameError: String?,
    onConfirmarClick: (String) -> Unit,
    onResetError: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    val esValido = username.length >= 3 && username.all { it.isLetterOrDigit() || it == '_' }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Tu @usuario",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Elige un nombre de usuario para tu perfil",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.secondary
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = username,
            onValueChange = {
                if (it.length <= 20) {
                    username = it.lowercase().trim()
                    if (usernameError != null) onResetError()
                }
            },
            label = { Text("Nombre de usuario") },
            placeholder = { Text("ej: viajero_24") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.AlternateEmail, contentDescription = null) },
            singleLine = true,
            isError = usernameError != null || (!esValido && username.isNotEmpty()),
            supportingText = {
                when {
                    usernameError != null -> Text(
                        text = usernameError,
                        color = MaterialTheme.colorScheme.error
                    )
                    !esValido && username.isNotEmpty() -> Text(
                        "Mínimo 3 caracteres, letras, números o guion bajo"
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { onConfirmarClick(username) },
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            enabled = esValido && authState !is AuthState.Cargando
        ) {
            if (authState is AuthState.Cargando) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text("Empezar mi aventura")
            }
        }

        if (authState is AuthState.Error) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = authState.mensaje,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
