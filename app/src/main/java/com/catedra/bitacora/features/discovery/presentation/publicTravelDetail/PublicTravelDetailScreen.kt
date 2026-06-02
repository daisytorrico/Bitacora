package com.catedra.bitacora.features.discovery.presentation.publicTravelDetail

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.catedra.bitacora.ui.components.AppTopBar
import com.catedra.bitacora.ui.components.TravelDetailContent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublicTravelDetailScreen(
    viewModel: PublicTravelDetailViewModel,
    onPointClick: (String, String) -> Unit,
    onProfileClick: (String) -> Unit,
    navController: NavController
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            AppTopBar(
                titulo = uiState.travel?.name ?: "Detalle",
                onBack = { navController.popBackStack() }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading && uiState.travel == null) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            TravelDetailContent(
                uiState = uiState,
                onProfileClick = { uiState.travel?.ownerId?.let { onProfileClick(it) } },
                onPointClick = onPointClick,
                paddingValues = paddingValues,
                statsActions = null
            )
        }
    }
}
