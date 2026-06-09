package com.catedra.bitacora.features.travel.presentation.travelList

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.catedra.bitacora.core.ui.components.common.AppTopBar
import com.catedra.bitacora.core.ui.components.common.BitacoraChip
import com.catedra.bitacora.core.ui.components.travel.TravelListContent
import com.catedra.bitacora.features.travel.domain.model.TravelStatus
import com.catedra.bitacora.features.travel.domain.model.TravelVisibility
import androidx.navigation.NavController
import com.catedra.bitacora.core.ui.theme.GrisMedio

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
    val lifecycleOwner = LocalLifecycleOwner.current
    var filterPanelVisible by remember { mutableStateOf(false) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.loadUserData()
                viewModel.loadTravels()
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
                titulo = "Tu perfil",
                actions = {
                    IconButton(onClick = { filterPanelVisible = !filterPanelVisible }) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Filtros",
                            tint = if (uiState.isFilterActive)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = onCerrarSesion) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Cerrar Sesión"
                        )
                    }
                }
            )
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
        if (uiState.loading && uiState.myTravels.isEmpty() && uiState.sharedTravels.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            var selectedTab by remember { mutableIntStateOf(0) }

            val filteredUiState = remember(uiState, selectedTab) {
                if (selectedTab == 0) {
                    uiState.copy(sharedTravels = emptyList())
                } else {
                    uiState.copy(myTravels = emptyList())
                }
            }

            TravelListContent(
                uiState = filteredUiState,
                travelCount = uiState.myTravels.size,
                onTravelClick = onTravelClick,
                onEditarPerfilClick = onEditarPerfilClick,
                paddingValues = paddingValues,
                middleContent = {
                    AnimatedVisibility(
                        visible = filterPanelVisible,
                        enter = expandVertically(),
                        exit = shrinkVertically()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            verticalArrangement = Arrangement.spacedBy((-8).dp)
                        ) {
                            Row(
                                modifier = Modifier.horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                listOf(
                                    TravelStatus.ONGOING to "En curso",
                                    TravelStatus.PLANNED to "Planificando",
                                    TravelStatus.COMPLETED to "Completado"
                                ).forEach { (status, label) ->
                                    val color = when (status) {
                                        TravelStatus.ONGOING -> Color(0xFF2E7D32)
                                        TravelStatus.PLANNED -> MaterialTheme.colorScheme.primary
                                        TravelStatus.COMPLETED -> GrisMedio
                                    }
                                    val selected = uiState.selectedStatus == status

                                    BitacoraChip(
                                        selected = selected,
                                        onClick = {
                                            viewModel.onStatusFilterChange(if (selected) null else status)
                                        },
                                        label = {
                                            Text(
                                                text = label,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold
                                            )
                                        },
                                        activeColor = color,
                                        unselectedBorderColor = color.copy(alpha = 0.5f),
                                        selectedContainerAlpha = 0.15f,
                                        selectedBorderAlpha = 0.5f,
                                        selectedBorderWidth = 1.dp,
                                        leadingIcon = if (selected) {
                                            { Box(Modifier.size(6.dp).background(color, CircleShape)) }
                                        } else null
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                listOf(
                                    TravelVisibility.PUBLIC to Icons.Outlined.Public,
                                    TravelVisibility.PRIVATE to Icons.Outlined.Lock,
                                    TravelVisibility.FOLLOWERS to Icons.Outlined.People
                                ).forEach { (visibility, icon) ->
                                    val selected = uiState.selectedVisibility == visibility

                                    BitacoraChip(
                                        selected = selected,
                                        onClick = {
                                            viewModel.onVisibilityFilterChange(if (selected) null else visibility)
                                        },
                                        label = {
                                            Icon(
                                                imageVector = icon,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp),
                                                tint = if (selected)
                                                    MaterialTheme.colorScheme.primary
                                                else
                                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                            )
                                        },
                                        selectedContainerAlpha = 0.1f,
                                        selectedBorderAlpha = 0.5f,
                                        selectedBorderWidth = 1.dp
                                    )
                                }
                            }
                        }
                    }

                    TabRow(selectedTabIndex = selectedTab) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = { Text("Mis viajes") }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = { Text("Compartidos") }
                        )
                    }
                }
            )
        }
    }
}