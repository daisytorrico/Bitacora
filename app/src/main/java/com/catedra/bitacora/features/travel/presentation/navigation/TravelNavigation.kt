package com.catedra.bitacora.features.travel.presentation.navigation

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.catedra.bitacora.features.travel.presentation.travelCreate.CreateTravelScreen
import com.catedra.bitacora.features.travel.presentation.travelList.TravelListScreen
import com.catedra.bitacora.features.travel.presentation.travelList.TravelListViewModel

object TravelDestinations {
    const val TRAVEL_LIST = "travel_list"
    const val TRAVEL_CREATE = "travel_create"
}

fun NavGraphBuilder.travelGraph(
    navController: NavController,
    onLogout: () -> Unit
) {
    // La pantalla de la lista de viajes (el "travel screen")
    composable(TravelDestinations.TRAVEL_LIST) {
        val viewModel: TravelListViewModel = hiltViewModel()
        TravelListScreen(
            viewModel = viewModel,
            onCerrarSesion = onLogout,
            onAgregarViajeClick = {
                navController.navigate(TravelDestinations.TRAVEL_CREATE)
            }
        )
    }

    // Pantalla del formulario para crear un viaje
    composable(TravelDestinations.TRAVEL_CREATE) {
        CreateTravelScreen(
            onBack = { navController.popBackStack() }
        )
    }
}
