package com.catedra.bitacora.ui.components

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.TravelExplore
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import com.catedra.bitacora.features.discovery.presentation.navigation.DiscoveryDestinations
import com.catedra.bitacora.features.map.presentation.navigation.MapDestination
import com.catedra.bitacora.features.travel.presentation.navigation.TravelDestinations

@Composable
fun AppBottomBar(
    currentDestination: NavDestination?,
    onNavigateToProfile: () -> Unit,
    onNavigateToExplorer: () -> Unit,
    onNavigateToMap: () -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        modifier = Modifier
            .navigationBarsPadding() 
            .height(80.dp)
    ) {
        // PERFIL
        NavigationBarItem(
            icon = { Icon(Icons.Default.Person, contentDescription = "Perfil") },
            label = { Text("Perfil") },
            selected = currentDestination?.hierarchy?.any { it.route == TravelDestinations.TRAVEL_LIST || it.route?.startsWith("travel") == true } == true,
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
            ),
            onClick = onNavigateToProfile
        )

        // EXPLORAR
        NavigationBarItem(
            icon = { Icon(Icons.Default.TravelExplore, contentDescription = "Explorar") },
            label = { Text("Explorar") },
            selected = currentDestination?.hierarchy?.any { it.route == "discovery_graph" || it.route == DiscoveryDestinations.EXPLORER || it.route?.contains("public") == true } == true,
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
            ),
            onClick = onNavigateToExplorer
        )

        // MAPA
        NavigationBarItem(
            icon = { Icon(Icons.Default.Map, contentDescription = "Mapa") },
            label = { Text("Mapa") },
            selected = currentDestination?.hierarchy?.any { it.route == MapDestination.MAP_VIEW } == true,
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
            ),
            onClick = onNavigateToMap
        )
    }
}
