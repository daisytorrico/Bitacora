package com.catedra.bitacora.features.travel.presentation.travelList

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.catedra.bitacora.ui.components.profile.ProfileHeader
import com.catedra.bitacora.ui.components.AppTopBar
import com.catedra.bitacora.ui.components.AppBottomBar
import com.catedra.bitacora.ui.components.TravelItem
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TravelListScreen(
    viewModel: TravelListViewModel,
    onCerrarSesion: () -> Unit,
    onAgregarViajeClick: () -> Unit,
    onEditarPerfilClick: () -> Unit = {},
    onTravelClick: (String) -> Unit = {},
    navController: NavController
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.loadUserData()
        viewModel.loadTravels()
    }

    Scaffold(
        topBar = {
            AppTopBar(
                titulo = "Mis Viajes",
                actions = {
                    IconButton(onClick = onCerrarSesion) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Cerrar Sesión"
                        )
                    }
                }
            )
        },
        bottomBar = {
            AppBottomBar(navController = navController)
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAgregarViajeClick,
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = Color.White
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Agregar viaje"
                )
            }
        }
    ) { paddingValues ->
        if (uiState.loading && uiState.travels.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Sección de Perfil
                item {
                    ProfileHeader(
                        user = uiState.user,
                        travelCount = uiState.filteredTravels.size,
                        onEditClick = onEditarPerfilClick
                    )
                }

                // Lista de Viajes
                items(
                    items = uiState.filteredTravels,
                    key = { it.id }
                ) { travel ->
                    Box {
                        TravelItem(
                            travel = travel,
                            pointsCount = travel.pointsCount,
                            onClick = { onTravelClick(travel.id) }
                        )
                    }
                }
            }
        }
    }
}
