package com.catedra.bitacora.features.travel.presentation.pointDetail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.catedra.bitacora.features.auth.presentation.navigation.AuthDestinations
import com.catedra.bitacora.ui.components.AppTopBar
import com.catedra.bitacora.ui.components.AppBottomBar
import com.catedra.bitacora.ui.components.UserHeader
import com.catedra.bitacora.ui.theme.GrisFondoApp
import com.catedra.bitacora.ui.theme.VerdeMentaFondo
import com.catedra.bitacora.ui.theme.VerdeMentaTexto
import com.catedra.bitacora.ui.theme.GrisClaro
import com.catedra.bitacora.ui.theme.GrisMedio
import com.catedra.bitacora.ui.theme.GrisOscuro
import com.catedra.bitacora.ui.theme.Blanco
import com.catedra.bitacora.ui.theme.Negro
import com.catedra.bitacora.ui.theme.RojoPin
import com.catedra.bitacora.ui.theme.GrisBorde
import com.catedra.bitacora.R

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
            uiState.point?.let { point ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Carrusel de Fotos Superior
                    if (point.imageUrls.isNotEmpty()) {
                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(240.dp)
                                .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)),
                            contentPadding = PaddingValues(horizontal = 0.dp)
                        ) {
                            items(point.imageUrls) { url ->
                                AsyncImage(
                                    model = url,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .fillParentMaxWidth()
                                        .height(240.dp),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(240.dp)
                                .background(MaterialTheme.colorScheme.background)
                                .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }

                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth()
                    ) {
                        // Bloque de Identificación del Creador (Componente reutilizable)
                        UserHeader(
                            user = uiState.creatorUser,
                            onClick = { navController.navigate(AuthDestinations.EDIT_PROFILE) }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Información Principal
                        Text(
                            text = point.name,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            Icon(
                                Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = GrisMedio,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = point.visitDate?.format(
                                    java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
                                ) ?: "Sin fecha",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, GrisBorde),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = GrisMedio
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = point.address,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Bloque Visual de Ubicación
                        Text(
                            text = "Ubicación",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.background)
                                .border(1.dp, GrisBorde, RoundedCornerShape(12.dp))
                        ) {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = "Pin",
                                tint = RojoPin,
                                modifier = Modifier
                                    .size(40.dp)
                                    .align(Alignment.Center)
                            )
                            OutlinedButton(
                                onClick = { /* Abrir Google Maps */ },
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(8.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(containerColor = Blanco)
                            ) {
                                Text("Ver en Maps", fontSize = 12.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Notas del Diario
                        Text(
                            text = "Notas del Diario",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, GrisBorde)
                        ) {
                            Text(
                                text = point.notes.ifEmpty { "No hay notas para este punto." },
                                modifier = Modifier.padding(16.dp),
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        // Botón Inferior
                        OutlinedButton(
                            onClick = { onEdit(point.id) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Editar Entrada")
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}
