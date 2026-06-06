package com.catedra.bitacora.features.travel.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navDeepLink
import com.catedra.bitacora.features.profile.presentation.navigation.ProfileDestinations
import com.catedra.bitacora.features.travel.presentation.pointCreate.CreatePointScreen
import com.catedra.bitacora.features.travel.presentation.pointDetail.PointDetailScreen
import com.catedra.bitacora.features.travel.presentation.pointEdit.EditPointScreen
import com.catedra.bitacora.features.travel.presentation.privileges.ManagePrivilegesScreen
import com.catedra.bitacora.features.travel.presentation.travelCreate.CreateTravelScreen
import com.catedra.bitacora.features.travel.presentation.travelDetail.TravelDetailScreen
import com.catedra.bitacora.features.travel.presentation.travelEdit.EditTravelScreen
import com.catedra.bitacora.features.travel.presentation.travelList.TravelListScreen
import com.catedra.bitacora.features.travel.presentation.travelList.TravelListViewModel


object TravelDestinations {
    const val TRAVEL_LIST = "travel_list"
    const val TRAVEL_CREATE = "travel_create"
    const val TRAVEL_EDIT = "travel_edit/{travelId}"
    const val TRAVEL_PRIVILEGES = "travel_privileges/{travelId}"
    const val TRAVEL_DETAIL = "travel_detail/{travelId}"
    const val TRAVEL_ADD_POINT = "travel_details/{travelId}/add_point"
    const val POINT_DETAIL = "travel_details/{travelId}/points/{pointId}"
    const val POINT_EDIT = "travel_details/{travelId}/points/{pointId}/edit"
    const val MAP = "map"
}

fun NavGraphBuilder.travelGraph(
    navController: NavController,
    onLogout: () -> Unit
) {
    composable(TravelDestinations.TRAVEL_LIST) {
        val viewModel: TravelListViewModel = hiltViewModel()
        TravelListScreen(
            viewModel = viewModel,
            onCerrarSesion = onLogout,
            onAgregarViajeClick = {
                navController.navigate(TravelDestinations.TRAVEL_CREATE)
            },
            onEditarPerfilClick = {
                navController.navigate(ProfileDestinations.EDIT_PROFILE)
            },
            onTravelClick = { travelId ->
                navController.navigate("travel_detail/$travelId")
            },
            navController = navController
        )
    }

    composable(
        route = TravelDestinations.TRAVEL_DETAIL,
        deepLinks = listOf(
            navDeepLink { uriPattern = "bitacora://travel_detail/{travelId}" }
        )
    ) {
        TravelDetailScreen(
            onBack = { navController.popBackStack() },
            onAddPointClick = { travelId ->
                navController.navigate("travel_details/$travelId/add_point")
            },
            onPointClick = { travelId, pointId ->
                navController.navigate("travel_details/$travelId/points/$pointId")
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

    composable(TravelDestinations.TRAVEL_EDIT) {
        EditTravelScreen(
            onBack = { navController.popBackStack() },
            onTravelUpdated = { navController.popBackStack() }
        )
    }

    composable(TravelDestinations.TRAVEL_PRIVILEGES) {
        ManagePrivilegesScreen(
            onBack = { navController.popBackStack() }
        )
    }

    composable(
        route = TravelDestinations.POINT_DETAIL,
        deepLinks = listOf(
            navDeepLink { uriPattern = "bitacora://travel_details/{travelId}/points/{pointId}" }
        )
    ) { backStackEntry ->
        val travelId = backStackEntry.arguments?.getString("travelId") ?: return@composable
        PointDetailScreen(
            onBack = { navController.popBackStack() },
            onEdit = { pointId ->
                navController.navigate("travel_details/$travelId/points/$pointId/edit")
            },
            navController = navController
        )
    }

    composable(TravelDestinations.POINT_EDIT) {
        EditPointScreen(
            onBack = { navController.popBackStack() },
            onPointUpdated = { navController.popBackStack() }
        )
    }

    composable(TravelDestinations.TRAVEL_CREATE) {
        CreateTravelScreen(
            onBack = { navController.popBackStack() },
            onTravelCreated = { travelId ->
                navController.navigate("travel_detail/$travelId") {
                    popUpTo(TravelDestinations.TRAVEL_CREATE) { inclusive = true }
                }
            }
        )
    }

    composable(TravelDestinations.TRAVEL_ADD_POINT) { backStackEntry ->
        val travelId = backStackEntry.arguments?.getString("travelId") ?: return@composable
        CreatePointScreen(
            onBack = { navController.popBackStack() },
            onPointCreated = { pointId ->
                navController.navigate("travel_details/$travelId/points/$pointId") {
                    popUpTo(TravelDestinations.TRAVEL_ADD_POINT) { inclusive = true }
                }
            }
        )
    }
}
