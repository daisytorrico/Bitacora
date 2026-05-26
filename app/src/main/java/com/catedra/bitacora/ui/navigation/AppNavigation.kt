package com.catedra.bitacora.ui.navigation

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.catedra.bitacora.features.auth.domain.model.AuthState
import com.catedra.bitacora.features.auth.presentation.AuthViewModel
import com.catedra.bitacora.features.auth.presentation.navigation.AuthDestinations
import com.catedra.bitacora.features.auth.presentation.navigation.authGraph
import com.catedra.bitacora.features.auth.presentation.util.crearGoogleSignInHandler
import com.catedra.bitacora.features.travel.presentation.navigation.TravelDestinations
import com.catedra.bitacora.features.travel.presentation.navigation.travelGraph
import com.catedra.bitacora.ui.components.AppBottomBar

object Rutas {
    const val LOGIN = AuthDestinations.LOGIN
    const val REGISTRO = AuthDestinations.REGISTRO
    const val USERNAME = AuthDestinations.USERNAME
    const val HOME = TravelDestinations.TRAVEL_LIST
}

@Composable
fun AppNavigation(viewModel: AuthViewModel) {
    val navController = rememberNavController()
    val authState by viewModel.authState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var showLogOut by remember { mutableStateOf(false) }

    val onGoogleSignInClick = crearGoogleSignInHandler(context, coroutineScope, viewModel)

    LaunchedEffect(authState) {
        val currentRoute = navController.currentDestination?.route
        when (authState) {
            is AuthState.Autenticado -> {
                if (currentRoute == Rutas.LOGIN || currentRoute == Rutas.REGISTRO) {
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
        authGraph(
            navController = navController,
            viewModel = viewModel,
            onGoogleSignInClick = onGoogleSignInClick
        )

        travelGraph(
            navController = navController,
            onLogout = { showLogOut = true }
        )
    }

    if (showLogOut) {
        AlertDialog(
            onDismissRequest = { showLogOut = false },
            title = { Text("Cerrar Sesión") },
            text = { Text("¿Seguro que desea cerrar sesión?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogOut = false
                        viewModel.cerrarSesion()
                    }
                ) {
                    Text("Cerrar sesión")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogOut = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
