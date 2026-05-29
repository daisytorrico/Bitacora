package com.catedra.bitacora.features.travel.presentation.travelDetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.catedra.bitacora.features.auth.presentation.navigation.AuthDestinations
import com.catedra.bitacora.ui.components.AppTopBar
import com.catedra.bitacora.ui.components.AppBottomBar
import com.catedra.bitacora.ui.components.PointOfInterestItem
import com.catedra.bitacora.ui.components.UserHeader
import com.catedra.bitacora.ui.theme.GrisPildora
import com.catedra.bitacora.ui.theme.GrisFondoApp
import com.catedra.bitacora.ui.theme.Blanco
import com.catedra.bitacora.ui.theme.Negro
import com.catedra.bitacora.ui.theme.VerdeMentaFondo
import com.catedra.bitacora.ui.theme.VerdeMentaTexto
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TravelDetailScreen(
    onBack: () -> Unit,
    onAddPointClick: (String) -> Unit,
    onPointClick: (String, String) -> Unit,
    navController: androidx.navigation.NavController, // Agregar esto
    viewModel: TravelDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val travel = uiState.travel
    val lifecycleOwner = LocalLifecycleOwner.current

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
                onBack = onBack
            )
        },
        bottomBar = { // Agregar la barra aquí
            AppBottomBar(navController = navController)
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { travel?.let { onAddPointClick(it.id) } },
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = Blanco
            ) {
                Icon(Icons.Default.Add, contentDescription = "Añadir punto")
            }
        }
    ) { paddingValues ->
        if (uiState.isLoading && travel == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (travel != null) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                // Item 1: Banner Principal
                item {
                    BannerSection(travel)
                }

                // Item 2: Cabecera del Creador y Píldoras
                item {
                    CreatorAndStatsSection(uiState) {
                        navController.navigate(AuthDestinations.EDIT_PROFILE)
                    }
                }

                // Item 3: Cabecera de Sección
                item {
                    Text(
                        text = "Puntos de interés",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        ),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                items(uiState.pointsOfInterest) { point ->
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        PointOfInterestItem(
                            point = point,
                            onClick = { travel?.let { onPointClick(it.id, point.id) } }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BannerSection(travel: com.catedra.bitacora.features.travel.domain.model.Travel) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
    ) {
        AsyncImage(
            model = travel.imageUrl,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Negro.copy(alpha = 0.7f)),
                        startY = 300f
                    )
                )
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = Blanco,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                val formatter = DateTimeFormatter.ofPattern("dd MMM")
                val dateText = if (travel.startDate != null && travel.endDate != null) {
                    "${travel.startDate.format(formatter)} - ${travel.endDate.format(formatter)}"
                } else "Sin fechas"
                Text(text = dateText, color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp)
            }
            Text(
                text = travel.description,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 16.sp,
                maxLines = 2
            )
        }
    }
}

@Composable
private fun CreatorAndStatsSection(uiState: TravelDetailUiState, onProfileClick: () -> Unit) {
    val travel = uiState.travel
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        UserHeader(
            user = uiState.creatorUser,
            avatarSize = 48,
            showBadges = true,
            onClick = onProfileClick,
            badges = {
                Badge(containerColor = MaterialTheme.colorScheme.surface) { 
                    Text("Privado", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface)
                }
                Badge(containerColor = MaterialTheme.colorScheme.secondaryContainer) { 
                    Text(travel?.status?.label ?: "Cargando", fontSize = 10.sp) 
                }
            }
        )

        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SuggestionChip(
                onClick = { },
                label = { Text("${uiState.pointsOfInterest.size} Puntos") },
                colors = SuggestionChipDefaults.suggestionChipColors(
                    containerColor = VerdeMentaFondo,
                    labelColor = VerdeMentaTexto
                ),
                shape = RoundedCornerShape(20.dp)
            )
        }
    }
}
