package com.catedra.bitacora.features.travel.presentation.travelDetail

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.catedra.bitacora.features.travel.presentation.navigation.TravelDestinations
import com.catedra.bitacora.ui.components.AppTopBar
import com.catedra.bitacora.ui.components.TravelDetailContent
import com.catedra.bitacora.ui.theme.Blanco

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TravelDetailScreen(
    onBack: () -> Unit,
    onAddPointClick: (String) -> Unit,
    onPointClick: (String, String) -> Unit,
    navController: androidx.navigation.NavController,
    viewModel: TravelDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val travel = uiState.travel
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.loadTravelDetails()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                titulo = travel?.name ?: "Detalle del Viaje",
                onBack = onBack
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { travel?.let { onAddPointClick(it.id) } },
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = Blanco
            ) {
                Icon(Icons.Default.Add, contentDescription = "Añadir punto")
            }
        }
    ) { paddingValues ->
        if (uiState.isLoading && travel == null) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (travel != null) {
            TravelDetailContent(
                uiState = uiState,
                onProfileClick = {
                    if (uiState.isOwner) {
                        navController.navigate(TravelDestinations.TRAVEL_LIST) {
                            popUpTo(TravelDestinations.TRAVEL_LIST) {
                                inclusive = false
                            }
                            launchSingleTop = true
                        }
                    }
                },
                onPointClick = onPointClick,
                paddingValues = paddingValues,
                statsActions = null
            )
        }
    }
}
