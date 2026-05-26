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
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.catedra.bitacora.features.travel.presentation.navigation.TravelDestinations
import com.catedra.bitacora.ui.theme.Blanco

@Composable
fun AppBottomBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    NavigationBar(
        containerColor = Blanco,
        tonalElevation = 0.dp,
        modifier = Modifier
            .navigationBarsPadding()
            .height(64.dp)
    ) {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Person, contentDescription = "Perfil") },
            label = { Text("Perfil") },
            selected = currentDestination?.hierarchy?.any { it.route == TravelDestinations.TRAVEL_LIST } == true,
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
            ),
            onClick = {
                navController.navigate(TravelDestinations.TRAVEL_LIST) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
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
                // Dejado sin funcionar por ahora
            }
        )
    }
}
