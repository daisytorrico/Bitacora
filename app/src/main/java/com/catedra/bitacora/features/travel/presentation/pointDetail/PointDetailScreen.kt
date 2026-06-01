package com.catedra.bitacora.features.travel.presentation.pointDetail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.catedra.bitacora.features.travel.presentation.navigation.TravelDestinations
import com.catedra.bitacora.ui.components.AppTopBar
import com.catedra.bitacora.ui.components.PointDetailContent

@Composable
fun PointDetailScreen(
    onBack: () -> Unit,
    onEdit: (String) -> Unit,
    navController: androidx.navigation.NavController,
    viewModel: PointDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            AppTopBar(
                titulo = uiState.point?.name ?: "Detalle del Punto",
                onBack = onBack
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.error != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Error: ${uiState.error}")
            }
        } else {
            PointDetailContent(
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
                onLikeClick = { viewModel.toggleLike() },
                onCommentsClick = { },
                paddingValues = paddingValues,
                actions = {
                    if (uiState.isOwner) {
                        Spacer(modifier = Modifier.height(32.dp))
                        OutlinedButton(
                            onClick = { uiState.point?.id?.let { onEdit(it) } },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Editar Entrada")
                        }
                    }
                }
            )
        }
    }
}
