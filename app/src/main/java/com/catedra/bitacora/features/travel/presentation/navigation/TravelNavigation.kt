package com.catedra.bitacora.features.travel.presentation.navigation

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.catedra.bitacora.features.travel.presentation.travelDetail.TravelDetailScreen
import com.catedra.bitacora.features.travel.presentation.travelDetail.TravelDetailViewModel

object TravelDestinations {
    const val TRAVEL_LIST = "travel_list"
}

fun NavGraphBuilder.travelGraph(
    navController: NavController,
    onLogout: () -> Unit
) {
    // La pantalla de la lista de viajes (el "travel screen")
    composable(TravelDestinations.TRAVEL_LIST) {
        val viewModel: TravelDetailViewModel = hiltViewModel()
        TravelDetailScreen(
            viewModel = viewModel,
            onCerrarSesion = onLogout
        )
    }
}
