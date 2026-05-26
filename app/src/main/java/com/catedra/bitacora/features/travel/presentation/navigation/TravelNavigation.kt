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
import com.catedra.bitacora.features.auth.presentation.navigation.AuthDestinations
import com.catedra.bitacora.features.travel.presentation.pointCreate.CreatePointScreen
import com.catedra.bitacora.features.travel.presentation.pointDetail.PointDetailScreen
import com.catedra.bitacora.features.travel.presentation.travelCreate.CreateTravelScreen
import com.catedra.bitacora.features.travel.presentation.travelDetail.TravelDetailScreen
import com.catedra.bitacora.features.travel.presentation.travelList.TravelListScreen
import com.catedra.bitacora.features.travel.presentation.travelList.TravelListViewModel
import com.catedra.bitacora.ui.components.AppTopBar
import com.catedra.bitacora.ui.components.AppBottomBar

object TravelDestinations {
    const val TRAVEL_LIST = "travel_list"
    const val TRAVEL_CREATE = "travel_create"
    const val TRAVEL_DETAIL = "travel_detail/{travelId}"
    const val TRAVEL_ADD_POINT = "travel_details/{travelId}/add_point"
    const val POINT_DETAIL = "travel_details/{travelId}/points/{pointId}"
    const val MAP = "map"
}

fun NavGraphBuilder.travelGraph(
    navController: NavController,
    onLogout: () -> Unit
) {
    // La pantalla de la lista de viajes
    composable(TravelDestinations.TRAVEL_LIST) {
        val viewModel: TravelListViewModel = hiltViewModel()
        TravelListScreen(
            viewModel = viewModel,
            onCerrarSesion = onLogout,
            onAgregarViajeClick = {
                navController.navigate(TravelDestinations.TRAVEL_CREATE)
            },
            onEditarPerfilClick = {
                navController.navigate(AuthDestinations.EDIT_PROFILE)
            },
            onTravelClick = { travelId ->
                navController.navigate("travel_detail/$travelId")
            },
            navController = navController
        )
    }

    // Pantalla de Detalle del Viaje
    composable(TravelDestinations.TRAVEL_DETAIL) {
        TravelDetailScreen(
            onBack = { navController.popBackStack() },
            onAddPointClick = { travelId ->
                navController.navigate("travel_details/$travelId/add_point")
            },
            onPointClick = { travelId, pointId ->
                navController.navigate("travel_details/$travelId/points/$pointId")
            },
            navController = navController // Pasar el navController
        )
    }

    // Pantalla de Detalle del Punto de Interés
    composable(TravelDestinations.POINT_DETAIL) {
        PointDetailScreen(
            onBack = { navController.popBackStack() },
            onEdit = { pointId ->
                // TODO: Navegar a editar punto
            },
            navController = navController // Pasar el navController
        )
    }

    // Pantalla del formulario para crear un viaje
    composable(TravelDestinations.TRAVEL_CREATE) {
        CreateTravelScreen(
            onBack = { navController.popBackStack() }
        )
    }

    // Pantalla para añadir un punto de interés
    composable(TravelDestinations.TRAVEL_ADD_POINT) {
        CreatePointScreen(
            onBack = { navController.popBackStack() }
        )
    }
}
