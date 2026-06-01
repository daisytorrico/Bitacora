package com.catedra.bitacora.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.catedra.bitacora.features.auth.domain.model.AuthState
import com.catedra.bitacora.features.auth.presentation.AuthViewModel
import com.catedra.bitacora.features.auth.presentation.navigation.AuthDestinations
import com.catedra.bitacora.features.auth.presentation.navigation.authGraph
import com.catedra.bitacora.features.auth.presentation.util.crearGoogleSignInHandler
import com.catedra.bitacora.features.discovery.presentation.navigation.discoveryGraph
import com.catedra.bitacora.features.map.presentation.navigation.MapDestination
import com.catedra.bitacora.features.map.presentation.navigation.mapGraph
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

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val showBottomBar = currentDestination?.hierarchy?.any { 
        it.route in listOf(Rutas.LOGIN, Rutas.REGISTRO, Rutas.USERNAME, AuthDestinations.EDIT_PROFILE, TravelDestinations.TRAVEL_CREATE, TravelDestinations.TRAVEL_ADD_POINT)
    } == false && authState is AuthState.Autenticado

    val animatedBottomPadding by animateDpAsState(
        targetValue = if (showBottomBar) 80.dp else 0.dp,
        label = "bottomBarPadding"
    )

    LaunchedEffect(authState) {
        val currentRoute = navController.currentDestination?.route
        when (authState) {
            is AuthState.Autenticado -> if (currentRoute in listOf(Rutas.LOGIN, Rutas.REGISTRO, Rutas.USERNAME)) {
                navController.navigate(Rutas.HOME) { popUpTo(0) { inclusive = true }; launchSingleTop = true }
            }
            is AuthState.NecesitaPerfil -> if (currentRoute != Rutas.USERNAME) {
                navController.navigate(Rutas.USERNAME) { popUpTo(0) { inclusive = true }; launchSingleTop = true }
            }
            is AuthState.NoAutenticado -> if (currentRoute !in listOf(Rutas.LOGIN, Rutas.REGISTRO)) {
                navController.navigate(Rutas.LOGIN) { popUpTo(0) { inclusive = true }; launchSingleTop = true }
            }
            else -> {}
        }
    }

    if (authState is AuthState.Cargando) {
        Box(modifier = Modifier.fillMaxSize())
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = when {
                authState is AuthState.Autenticado -> Rutas.HOME
                authState is AuthState.NecesitaPerfil || authState is AuthState.EsperandoUsername -> Rutas.USERNAME
                viewModel.estaLogueado() -> Rutas.USERNAME
                else -> Rutas.LOGIN
            },
            modifier = Modifier.padding(bottom = animatedBottomPadding)
        ) {
            authGraph(navController, viewModel, crearGoogleSignInHandler(context, coroutineScope, viewModel))
            travelGraph(navController) { showLogOut = true }
            discoveryGraph(navController)
            mapGraph(navController) { showLogOut = true }
        }

        AnimatedVisibility(
            visible = showBottomBar,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it })
        ) {
            AppBottomBar(
                currentDestination = currentDestination,
                onNavigateToProfile = {
                    navController.navigate(TravelDestinations.TRAVEL_LIST) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateToExplorer = {
                    navController.navigate("discovery_graph") {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateToMap = {
                    navController.navigate(MapDestination.MAP_VIEW) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }

    if (showLogOut) {
        AlertDialog(
            onDismissRequest = { showLogOut = false },
            title = { Text("Cerrar Sesión") },
            text = { Text("¿Seguro que desea cerrar sesión?") },
            confirmButton = {
                TextButton(onClick = { showLogOut = false; viewModel.cerrarSesion() }) {
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
