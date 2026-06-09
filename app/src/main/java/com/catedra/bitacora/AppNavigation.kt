package com.catedra.bitacora

import androidx.compose.animation.*
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.catedra.bitacora.core.domain.model.AuthState
import com.catedra.bitacora.features.auth.presentation.AuthViewModel
import com.catedra.bitacora.features.auth.presentation.navigation.AuthDestinations
import com.catedra.bitacora.features.auth.presentation.navigation.authGraph
import com.catedra.bitacora.features.auth.presentation.util.createGoogleSignInHandler
import com.catedra.bitacora.features.profile.presentation.navigation.ProfileDestinations
import com.catedra.bitacora.features.profile.presentation.navigation.profileGraph
import com.catedra.bitacora.features.discovery.presentation.navigation.discoveryGraph
import com.catedra.bitacora.features.social.presentation.navigation.socialGraph
import com.catedra.bitacora.features.map.presentation.navigation.MapDestination
import com.catedra.bitacora.features.map.presentation.navigation.mapGraph
import com.catedra.bitacora.features.travel.presentation.navigation.TravelDestinations
import com.catedra.bitacora.features.travel.presentation.navigation.travelGraph
import com.catedra.bitacora.core.ui.components.common.AppBottomBar
import com.catedra.bitacora.features.discovery.presentation.navigation.DiscoveryDestinations

object Rutas {
    const val LOGIN = AuthDestinations.LOGIN
    const val REGISTRO = AuthDestinations.REGISTRO
    const val USERNAME = AuthDestinations.USERNAME
    const val HOME = TravelDestinations.TRAVEL_LIST
    const val DISCOVERY_GRAPH = DiscoveryDestinations.DISCOVERY_GRAPH
}

@Composable
fun AppNavigation(viewModel: AuthViewModel) {
    // Declaramos todo al principio para evitar advertencias de "código inalcanzable"
    val navController = rememberNavController()
    val authState by viewModel.authState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var showLogOut by remember { mutableStateOf(false) }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Si está cargando, mostramos el spinner y salimos. 
    // Al estar declarado showLogOut arriba, ya no habrá advertencias.
    if (authState is AuthState.Cargando) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        return
    }

    val showBottomBar = currentDestination?.hierarchy?.any { 
        it.route in listOf(
            Rutas.LOGIN, 
            Rutas.REGISTRO, 
            Rutas.USERNAME, 
            ProfileDestinations.EDIT_PROFILE, 
            TravelDestinations.TRAVEL_CREATE, 
            TravelDestinations.TRAVEL_ADD_POINT
        )
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

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = when (authState) {
                is AuthState.Autenticado -> Rutas.HOME
                is AuthState.NecesitaPerfil, is AuthState.EsperandoUsername -> Rutas.USERNAME
                else -> Rutas.LOGIN
            },
            modifier = Modifier.padding(bottom = animatedBottomPadding)
        ) {
            authGraph(navController, viewModel, createGoogleSignInHandler(context, coroutineScope, viewModel))
            travelGraph(navController) { showLogOut = true }
            discoveryGraph(navController)
            profileGraph(navController)
            socialGraph(navController)
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
                    val rutaActual = navController.currentDestination?.route
                    if (rutaActual != TravelDestinations.TRAVEL_LIST) {
                        val onProfileHierarchy = currentDestination?.hierarchy?.any { it.route?.startsWith("travel") == true } == true
                        navController.navigate(TravelDestinations.TRAVEL_LIST) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = !onProfileHierarchy
                        }
                    }
                },
                onNavigateToExplorer = {
                    val rutaActual = navController.currentDestination?.route
                    if (rutaActual != Rutas.DISCOVERY_GRAPH) {
                        val onExplorerHierarchy = currentDestination?.hierarchy?.any { it.route == Rutas.DISCOVERY_GRAPH } == true
                        navController.navigate(Rutas.DISCOVERY_GRAPH) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = !onExplorerHierarchy
                        }
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
            title = { Text(stringResource(R.string.close_session)) },
            text = { Text(stringResource(R.string.close_session_confirm)) },
            confirmButton = {
                TextButton(onClick = { showLogOut = false; viewModel.logout() }) {
                    Text(stringResource(R.string.close_session))
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogOut = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}
