package com.catedra.bitacora.features.discovery.presentation.publicTravelDetail

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.catedra.bitacora.core.ui.components.common.AppTopBar
import com.catedra.bitacora.core.ui.components.travel.TravelDetailContent
import com.catedra.bitacora.core.ui.theme.Blanco

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublicTravelDetailScreen(
    viewModel: PublicTravelDetailViewModel,
    onPointClick: (String, String) -> Unit,
    onProfileClick: (String) -> Unit,
    onEditClick: (String) -> Unit,
    onPrivilegesClick: (String) -> Unit,
    onAddPointClick: (String) -> Unit,
    navController: NavController
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            AppTopBar(
                titulo = uiState.travel?.name ?: "Detalle",
                onBack = { navController.popBackStack() },
                actions = {
                    if (uiState.isOwner) {
                        IconButton(onClick = { uiState.travel?.id?.let { onPrivilegesClick(it) } }) {
                            Icon(Icons.Default.Group, contentDescription = "Privilegios")
                        }
                    }
                    if (uiState.canEdit) {
                        IconButton(onClick = { uiState.travel?.id?.let { onEditClick(it) } }) {
                            Icon(Icons.Default.Edit, contentDescription = "Editar")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (uiState.canEdit) {
                FloatingActionButton(
                    onClick = { uiState.travel?.id?.let { onAddPointClick(it) } },
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = Blanco
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Añadir punto")
                }
            }
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
