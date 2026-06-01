package com.catedra.bitacora.ui.components

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.ui.graphics.Color
import com.catedra.bitacora.features.map.presentation.navigation.MapDestination
import com.catedra.bitacora.features.travel.presentation.navigation.TravelDestinations
import com.catedra.bitacora.ui.theme.Blanco

@Composable
fun AppBottomBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp
    ) {
        NavigationBar(
            containerColor = Color.Transparent,
            tonalElevation = 0.dp,
            windowInsets = NavigationBarDefaults.windowInsets
        ) {
            NavigationBarItem(
                icon = { Icon(Icons.Default.Person, contentDescription = "Perfil") },
                label = { Text("Perfil") },
                selected = currentDestination?.hierarchy?.any { destination ->
                    destination.route == TravelDestinations.TRAVEL_LIST ||
                            destination.route == TravelDestinations.TRAVEL_DETAIL ||
                            destination.route == TravelDestinations.POINT_DETAIL
                } == true,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
                ),
                onClick = {
                    if (currentDestination?.route != TravelDestinations.TRAVEL_LIST) {
                        navController.navigate(TravelDestinations.TRAVEL_LIST) {
                            popUpTo(0) {
                                inclusive = false
                            }
                            launchSingleTop = true
                        }
                    }
                }
            )
            NavigationBarItem(
                icon = { Icon(Icons.Default.Map, contentDescription = "Mapa") },
                label = { Text("Mapa") },
                selected = currentDestination?.hierarchy?.any { it.route == TravelDestinations.MAP } == true,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
                ),
                onClick = {
                    if (currentDestination?.route != MapDestination.MAP_VIEW) {
                        navController.navigate(MapDestination.MAP_VIEW) {
                            launchSingleTop = true
                        }
                    }
                }
            )
        }
    }
}
