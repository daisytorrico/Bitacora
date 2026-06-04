package com.catedra.bitacora.features.travel.presentation.pointDetail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.catedra.bitacora.core.domain.model.Coordinates
import com.catedra.bitacora.core.domain.model.PointOnMap
import com.catedra.bitacora.features.profile.presentation.navigation.ProfileDestinations
import com.catedra.bitacora.core.ui.components.common.AppTopBar
import com.catedra.bitacora.core.ui.components.form.LocationViewer
import com.catedra.bitacora.core.ui.components.travel.PointDetailContent

@Composable
fun PointDetailScreen(
    onBack: () -> Unit,
    onEdit: (String) -> Unit,
    navController: androidx.navigation.NavController,
    viewModel: PointDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            AppTopBar(
                titulo = uiState.point?.name ?: "Detalle del Punto",
                onBack = onBack
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        if (uiState.isLoading && uiState.point == null) {
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
                onProfileClick = { navController.navigate(ProfileDestinations.EDIT_PROFILE) },
                onLikeClick = { viewModel.toggleLike() },
                onCommentsClick = { /* TODO: Implementar comentarios */ },
                onToggleMap = { viewModel.onToggleMap(it) },
                paddingValues = paddingValues,
                actions = {
                    if (uiState.canEdit) {
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

        if (uiState.showMap && uiState.point != null) {
            val point = uiState.point!!
            if (point.latitude != null && point.longitude != null) {
                LocationViewer(
                    point = PointOnMap(
                        name = point.name,
                        address = point.address,
                        coordinates = Coordinates(point.latitude, point.longitude)
                    ),
                    onDismiss = { viewModel.onToggleMap(false) }
                )
            }
        }
    }
}
