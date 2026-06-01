package com.catedra.bitacora.features.map.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.catedra.bitacora.features.map.presentation.MapScreen

object MapDestination {
    const val MAP_VIEW = "map_screen"
}

fun NavGraphBuilder.mapGraph(
    navController: NavController,
    onLogout: () -> Unit
) {
    composable(MapDestination.MAP_VIEW) {
        MapScreen(
            navController = navController,
            onLogout = onLogout,
            onNavigateToPoi = { travelId, poiId ->
                navController.navigate("travel_detail/$travelId")
                navController.navigate("travel_details/$travelId/points/$poiId")
            }
        )
    }
}
