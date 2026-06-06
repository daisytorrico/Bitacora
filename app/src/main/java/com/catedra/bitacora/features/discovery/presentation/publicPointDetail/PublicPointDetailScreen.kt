package com.catedra.bitacora.features.discovery.presentation.publicPointDetail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.catedra.bitacora.features.social.presentation.comments.CommentsSheetContent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublicPointDetailScreen(
    viewModel: PublicPointDetailViewModel,
    onProfileClick: (String) -> Unit,
    navController: NavController
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showComments by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            AppTopBar(
                titulo = uiState.point?.name ?: "Punto de Interés",
                onBack = { navController.popBackStack() }
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
                onCommentsClick = { showComments = true },
                onToggleMap = { viewModel.onToggleMap(it) },
                paddingValues = paddingValues,
                actions = null
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
                    isTripOwner = false,
                    onProfileClick = { userId ->
                        showComments = false
                        onProfileClick(userId)
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