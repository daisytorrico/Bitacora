package com.catedra.bitacora.core.ui.components.common

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.TravelExplore
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import com.catedra.bitacora.R
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
            icon = { Icon(Icons.Default.Person, contentDescription = stringResource(R.string.profile)) },
            label = { Text(stringResource(R.string.profile)) },
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
            icon = { Icon(Icons.Default.TravelExplore, contentDescription = stringResource(R.string.explore)) },
            label = { Text(stringResource(R.string.explore)) },
            selected = currentDestination?.hierarchy?.any { it.route == DiscoveryDestinations.DISCOVERY_GRAPH || it.route == DiscoveryDestinations.EXPLORER || it.route?.contains("public") == true } == true,
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
            ),
            onClick = onNavigateToExplorer
        )

        // MAPA
        NavigationBarItem(
            icon = { Icon(Icons.Default.Map, contentDescription = stringResource(R.string.map)) },
            label = { Text(stringResource(R.string.map)) },
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
