package com.catedra.bitacora.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.catedra.bitacora.ui.auth.AuthState
import com.catedra.bitacora.ui.auth.AuthViewModel
import com.catedra.bitacora.ui.auth.LoginScreen
import com.catedra.bitacora.ui.auth.RegisterScreen
import com.catedra.bitacora.ui.auth.UsernameScreen
import com.catedra.bitacora.ui.auth.crearGoogleSignInHandler
import com.catedra.bitacora.ui.home.HomeScreen

object Rutas {
    const val LOGIN = "login"
    const val REGISTRO = "registro"
    const val USERNAME = "username"
    const val HOME = "home"
}

@Composable
fun AppNavigation(viewModel: AuthViewModel) {
    val navController = rememberNavController()
    val authState by viewModel.authState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val onGoogleSignInClick = crearGoogleSignInHandler(context, coroutineScope, viewModel)

    LaunchedEffect(authState) {
        val currentRoute = navController.currentDestination?.route
        when (authState) {
            is AuthState.Autenticado -> {
                if (currentRoute != Rutas.HOME) {
                    navController.navigate(Rutas.HOME) {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }
            is AuthState.NecesitaPerfil -> {
                if (currentRoute != Rutas.USERNAME) {
                    navController.navigate(Rutas.USERNAME) {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }
            is AuthState.NoAutenticado -> {
                if (currentRoute != Rutas.LOGIN && currentRoute != Rutas.REGISTRO) {
                    navController.navigate(Rutas.LOGIN) {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }
            else -> {}
        }
    }

    NavHost(
        navController = navController,
        startDestination = when {
            authState is AuthState.Autenticado -> Rutas.HOME
            authState is AuthState.NecesitaPerfil || authState is AuthState.EsperandoUsername -> Rutas.USERNAME
            viewModel.estaLogueado() -> Rutas.USERNAME
            else -> Rutas.LOGIN
        }
    ) {
        composable(Rutas.LOGIN) {
            LoginScreen(
                authState = authState,
                onLoginClick = { email, pass -> viewModel.iniciarSesion(email, pass) },
                onGoogleSignInClick = onGoogleSignInClick,
                onNavigateToRegister = { navController.navigate(Rutas.REGISTRO) }
            )
        }

        composable(Rutas.REGISTRO) {
            RegisterScreen(
                authState = authState,
                onRegisterClick = { nombre, email, pass -> viewModel.registrar(nombre, email, pass) },
                onNavigateToLogin = { navController.popBackStack() }
            )
        }

        composable(Rutas.USERNAME) {
            val usernameError by viewModel.usernameError.collectAsStateWithLifecycle()
            UsernameScreen(
                authState = authState,
                usernameError = usernameError,
                onConfirmarClick = { username -> viewModel.guardarUsername(username) },
                onResetError = { viewModel.limpiarUsernameError() }
            )
        }

        composable(Rutas.HOME) {
            HomeScreen(
                onCerrarSesion = { viewModel.cerrarSesion() }
            )
        }
    }
}
