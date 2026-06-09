package com.catedra.bitacora.features.travel.presentation.travelDetail

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.catedra.bitacora.features.travel.presentation.navigation.TravelDestinations
import com.catedra.bitacora.core.ui.components.common.AppTopBar
import com.catedra.bitacora.core.ui.components.travel.TravelDetailContent
import com.catedra.bitacora.core.ui.theme.Blanco

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TravelDetailScreen(
    onBack: () -> Unit,
    onAddPointClick: (String) -> Unit,
    onPointClick: (String, String) -> Unit,
    onEditClick: (String) -> Unit,
    onPrivilegesClick: (String) -> Unit,
    navController: androidx.navigation.NavController,
    viewModel: TravelDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val travel = uiState.travel
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current

    LaunchedEffect(uiState.isDeleted) {
        if (uiState.isDeleted) {
            Toast.makeText(context, "Viaje eliminado", Toast.LENGTH_SHORT).show()
            onBack()
        }
    }

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
                onBack = onBack,
                actions = {
                    if (uiState.isOwner) {
                        IconButton(onClick = { travel?.id?.let { onPrivilegesClick(it) } }) {
                            Icon(Icons.Default.Group, contentDescription = "Privilegios")
                        }
                    }
                    if (uiState.canEdit) {
                        IconButton(onClick = { travel?.id?.let { onEditClick(it) } }) {
                            Icon(Icons.Default.Edit, contentDescription = "Editar")
                        }
                    }
                    if (uiState.isOwner) {
                        var showMenu by remember { mutableStateOf(false) }
                        Box {
                            IconButton(onClick = { showMenu = !showMenu }) {
                                Icon(Icons.Default.MoreVert, contentDescription = "Más opciones")
                            }
                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false },
                                modifier = Modifier.width(180.dp),
                                containerColor = MaterialTheme.colorScheme.surface
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Borrar viaje", modifier = Modifier.fillMaxWidth()) },
                                    onClick = {
                                        showMenu = false
                                        viewModel.setShowDeleteDialog(true)
                                    }
                                )
                            }
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (uiState.canEdit) {
                FloatingActionButton(
                    onClick = { travel?.let { onAddPointClick(it.id) } },
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = Blanco
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Añadir punto")
                }
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

        if (uiState.showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.setShowDeleteDialog(false) },
                title = { Text("¿Borrar viaje?") },
                text = { Text("Esta acción borrará el viaje, todos sus puntos de interés, comentarios y likes de forma permanente.") },
                confirmButton = {
                    TextButton(
                        onClick = { viewModel.deleteTravel() },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Borrar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.setShowDeleteDialog(false) }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}
