package com.catedra.bitacora.features.auth.presentation.navigation

import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.catedra.bitacora.features.auth.presentation.AuthViewModel
import com.catedra.bitacora.features.auth.presentation.login.LoginScreen
import com.catedra.bitacora.features.auth.presentation.register.RegisterScreen
import com.catedra.bitacora.features.auth.presentation.username.UsernameScreen

object AuthDestinations {
    const val LOGIN = "login"
    const val REGISTRO = "registro"
    const val USERNAME = "username"
}

/**
 * Extensión de NavGraphBuilder para encapsular las pantallas de Auth.
 * AppNavigation solo llama a esta función.
 */
fun NavGraphBuilder.authGraph(
    navController: NavController,
    viewModel: AuthViewModel,
    onGoogleSignInClick: () -> Unit
) {
    composable(AuthDestinations.LOGIN) {
        val authState by viewModel.authState.collectAsStateWithLifecycle()
        LoginScreen(
            authState = authState,
            onLoginClick = { email, pass -> viewModel.iniciarSesion(email, pass) },
            onGoogleSignInClick = onGoogleSignInClick,
            onNavigateToRegister = { navController.navigate(AuthDestinations.REGISTRO) }
        )
    }

    composable(AuthDestinations.REGISTRO) {
        val authState by viewModel.authState.collectAsStateWithLifecycle()
        RegisterScreen(
            authState = authState,
            onRegisterClick = { nombre, email, pass -> viewModel.registrar(nombre, email, pass) },
            onNavigateToLogin = { navController.popBackStack() }
        )
    }

    composable(AuthDestinations.USERNAME) {
        val authState by viewModel.authState.collectAsStateWithLifecycle()
        val usernameError by viewModel.usernameError.collectAsStateWithLifecycle()
        UsernameScreen(
            authState = authState,
            usernameError = usernameError,
            onConfirmarClick = { username -> viewModel.guardarUsername(username) },
            onResetError = { viewModel.limpiarUsernameError() }
        )
    }
}
