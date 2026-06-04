package com.catedra.bitacora.features.discovery.presentation.publicPointDetail

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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.catedra.bitacora.core.domain.model.Coordinates
import com.catedra.bitacora.core.domain.model.PointOnMap
import com.catedra.bitacora.core.ui.components.common.AppTopBar
import com.catedra.bitacora.core.ui.components.form.LocationViewer
import com.catedra.bitacora.core.ui.components.travel.PointDetailContent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublicPointDetailScreen(
    viewModel: PublicPointDetailViewModel,
    onProfileClick: (String) -> Unit,
    onEditClick: (String, String) -> Unit,
    navController: NavController
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            AppTopBar(
                titulo = uiState.point?.name ?: "Punto de Interés",
                onBack = { navController.popBackStack() },
                actions = {
                    if (uiState.canEdit) {
                        IconButton(onClick = { uiState.point?.id?.let { onEditClick(viewModel.travelId, it) } }) {
                            Icon(Icons.Default.Edit, contentDescription = "Editar")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading && uiState.point == null) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            PointDetailContent(
                uiState = uiState,
                onProfileClick = { uiState.creatorUser?.uid?.let { onProfileClick(it) } },
                onLikeClick = { viewModel.toggleLike() },
                onCommentsClick = { },
                onToggleMap = { viewModel.onToggleMap(it) },
                paddingValues = paddingValues
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
