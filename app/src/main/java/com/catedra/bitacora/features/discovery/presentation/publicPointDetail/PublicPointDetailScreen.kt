package com.catedra.bitacora.features.discovery.presentation.publicPointDetail

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.catedra.bitacora.ui.components.AppTopBar
import com.catedra.bitacora.ui.components.PointDetailContent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublicPointDetailScreen(
    viewModel: PublicPointDetailViewModel,
    onProfileClick: (String) -> Unit,
    navController: NavController
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

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
                onCommentsClick = { },
                paddingValues = paddingValues,
                actions = null
            )
        }
    }
}
