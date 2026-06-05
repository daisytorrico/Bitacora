package com.catedra.bitacora.features.travel.presentation.pointDetail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.catedra.bitacora.core.domain.model.Coordinates
import com.catedra.bitacora.core.domain.model.PointOnMap
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.catedra.bitacora.features.profile.presentation.navigation.ProfileDestinations
import com.catedra.bitacora.features.social.presentation.comments.CommentsSheetContent
import com.catedra.bitacora.features.travel.presentation.navigation.TravelDestinations
import com.catedra.bitacora.core.ui.components.common.AppTopBar
import com.catedra.bitacora.core.ui.components.form.LocationViewer
import com.catedra.bitacora.core.ui.components.travel.PointDetailContent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PointDetailScreen(
    onBack: () -> Unit,
    onEdit: (String) -> Unit,
    navController: androidx.navigation.NavController,
    viewModel: PointDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showComments by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            AppTopBar(
                titulo = uiState.point?.name ?: "Detalle del Punto",
                onBack = onBack,
                actions = {
                    if (uiState.canEdit) {
                        IconButton(onClick = { uiState.point?.id?.let { onEdit(it) } }) {
                            Icon(Icons.Default.Edit, contentDescription = "Editar")
                        }
                    }
                }
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
                onCommentsClick = { showComments = true },
                onToggleMap = { viewModel.onToggleMap(it) },
                paddingValues = paddingValues
            )
        }

        if (showComments) {
            ModalBottomSheet(
                onDismissRequest = { showComments = false },
                sheetState = sheetState,
                dragHandle = { BottomSheetDefaults.DragHandle() },
                containerColor = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            ) {
                CommentsSheetContent(
                    currentUserId = uiState.currentUserId,
                    isTripOwner = uiState.isOwner,
                    onProfileClick = { userId ->
                        showComments = false
                        if (userId == uiState.currentUserId) {
                            navController.navigate(com.catedra.bitacora.features.travel.presentation.navigation.TravelDestinations.TRAVEL_LIST) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        } else {
                            navController.navigate("public_profile/$userId")
                        }
                    }
                )
            }
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
