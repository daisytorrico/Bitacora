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
import com.catedra.bitacora.ui.auth.crearGoogleSignInHandler
import com.catedra.bitacora.ui.home.HomeScreen

object Rutas {
    const val LOGIN = "login"
    const val REGISTRO = "registro"
    const val HOME = "home"
}

@Composable
fun AppNavigation(viewModel: AuthViewModel) {
    val navController = rememberNavController()
    val authState by viewModel.authState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Usamos el helper para mantener limpia la navegación
    val onGoogleSignInClick = crearGoogleSignInHandler(context, coroutineScope, viewModel)

    // Efecto para reaccionar a cambios de autenticación
    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Autenticado -> {
                navController.navigate(Rutas.HOME) {
                    popUpTo(0) { inclusive = true }
                    launchSingleTop = true
                }
            }
            is AuthState.NoAutenticado -> {
                if (navController.currentDestination?.route != Rutas.LOGIN &&
                    navController.currentDestination?.route != Rutas.REGISTRO) {
                    navController.navigate(Rutas.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
            is AuthState.Error -> {
                // El error ya se muestra en la UI a traves del authState
            }
            is AuthState.Cargando -> {}
        }
    }

    NavHost(
        navController = navController,
        startDestination = if (authState is AuthState.Autenticado) Rutas.HOME else Rutas.LOGIN
    ) {
        composable(Rutas.LOGIN) {
            if (authState !is AuthState.Cargando) {
                LoginScreen(
                    authState = authState,
                    onLoginClick = { email, pass -> viewModel.iniciarSesion(email, pass) },
                    onGoogleSignInClick = onGoogleSignInClick,
                    onNavigateToRegister = { navController.navigate(Rutas.REGISTRO) }
                )
            }
        }

        composable(Rutas.REGISTRO) {
            RegisterScreen(
                authState = authState,
                onRegisterClick = { email, pass -> viewModel.registrar(email, pass) },
                onNavigateToLogin = { navController.popBackStack() }
            )
        }

        composable(Rutas.HOME) {
            HomeScreen(
                onCerrarSesion = { viewModel.cerrarSesion() }
            )
        }
    }
}
