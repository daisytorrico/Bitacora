package com.catedra.bitacora.features.discovery.presentation.navigation

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.catedra.bitacora.features.discovery.presentation.allPublicTravels.AllPublicTravelsScreen
import com.catedra.bitacora.features.discovery.presentation.allPublicTravels.AllPublicTravelsViewModel
import com.catedra.bitacora.features.discovery.presentation.explorer.ExplorerScreen
import com.catedra.bitacora.features.discovery.presentation.explorer.ExplorerViewModel
import com.catedra.bitacora.features.discovery.presentation.publicPointDetail.PublicPointDetailScreen
import com.catedra.bitacora.features.discovery.presentation.publicPointDetail.PublicPointDetailViewModel
import com.catedra.bitacora.features.discovery.presentation.publicProfile.PublicProfileScreen
import com.catedra.bitacora.features.discovery.presentation.publicProfile.PublicProfileViewModel
import com.catedra.bitacora.features.discovery.presentation.publicTravelDetail.PublicTravelDetailScreen
import com.catedra.bitacora.features.discovery.presentation.publicTravelDetail.PublicTravelDetailViewModel

fun NavGraphBuilder.discoveryGraph(navController: NavController) {
    navigation(
        startDestination = DiscoveryDestinations.EXPLORER,
        route = "discovery_graph"
    ) {
        composable(DiscoveryDestinations.EXPLORER) {
            val viewModel: ExplorerViewModel = hiltViewModel()
            ExplorerScreen(
                viewModel = viewModel,
                onTravelClick = { travelId ->
                    navController.navigate(DiscoveryDestinations.publicTravelDetail(travelId))
                },
                onSeeAllClick = {
                    navController.navigate(DiscoveryDestinations.ALL_PUBLIC_TRAVELS)
                },
                navController = navController
            )
        }

        composable(DiscoveryDestinations.ALL_PUBLIC_TRAVELS) {
            val viewModel: AllPublicTravelsViewModel = hiltViewModel()
            AllPublicTravelsScreen(
                viewModel = viewModel,
                onTravelClick = { travelId ->
                    navController.navigate(DiscoveryDestinations.publicTravelDetail(travelId))
                },
                navController = navController
            )
        }

        composable(DiscoveryDestinations.PUBLIC_PROFILE) {
            val viewModel: PublicProfileViewModel = hiltViewModel()
            PublicProfileScreen(
                viewModel = viewModel,
                onTravelClick = { travelId ->
                    navController.navigate(DiscoveryDestinations.publicTravelDetail(travelId))
                },
                navController = navController
            )
        }

        composable(DiscoveryDestinations.PUBLIC_TRAVEL_DETAIL) {
            val viewModel: PublicTravelDetailViewModel = hiltViewModel()
            PublicTravelDetailScreen(
                viewModel = viewModel,
                onPointClick = { travelId, pointId ->
                    val ownerId = viewModel.uiState.value.travel?.ownerId ?: ""
                    navController.navigate(DiscoveryDestinations.publicPointDetail(travelId, pointId, ownerId))
                },
                onProfileClick = { userId ->
                    navController.navigate(DiscoveryDestinations.publicProfile(userId))
                },
                onEditClick = { travelId ->
                    navController.navigate("travel_edit/$travelId")
                },
                onPrivilegesClick = { travelId ->
                    navController.navigate("travel_privileges/$travelId")
                },
                navController = navController
            )
        }

        composable(DiscoveryDestinations.PUBLIC_POINT_DETAIL) {
            val viewModel: PublicPointDetailViewModel = hiltViewModel()
            PublicPointDetailScreen(
                viewModel = viewModel,
                onProfileClick = { userId ->
                    navController.navigate(DiscoveryDestinations.publicProfile(userId))
                },
                onEditClick = { travelId, pointId ->
                    navController.navigate("travel_details/$travelId/points/$pointId/edit")
                },
                navController = navController
            )
        }
    }
}
