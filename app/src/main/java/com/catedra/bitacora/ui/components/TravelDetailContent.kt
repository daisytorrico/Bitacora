package com.catedra.bitacora.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.catedra.bitacora.features.travel.domain.model.Travel
import com.catedra.bitacora.features.travel.presentation.travelDetail.TravelDetailUiState
import com.catedra.bitacora.ui.theme.Blanco
import com.catedra.bitacora.ui.theme.Negro
import com.catedra.bitacora.ui.theme.VerdeMentaFondo
import com.catedra.bitacora.ui.theme.VerdeMentaTexto
import java.time.format.DateTimeFormatter

@Composable
fun TravelDetailContent(
    uiState: TravelDetailUiState,
    onProfileClick: () -> Unit,
    onPointClick: (String, String) -> Unit,
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues = PaddingValues(0.dp),
    statsActions: @Composable (RowScope.() -> Unit)? = null
) {
    val travel = uiState.travel ?: return

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(paddingValues)
            .background(MaterialTheme.colorScheme.background),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        item {
            BannerSection(travel)
        }

        item {
            CreatorAndStatsSection(uiState, onProfileClick, statsActions)
        }

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
                    onClick = { onPointClick(travel.id, point.id) }
                )
            }
        }
    }
}

@Composable
private fun BannerSection(travel: Travel) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .background(com.catedra.bitacora.ui.theme.GrisSeparador)
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
                Text(text = dateText, color = Blanco, fontSize = 14.sp)
            }
            Text(
                text = travel.description,
                color = Blanco,
                fontSize = 16.sp,
                maxLines = 2
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreatorAndStatsSection(
    uiState: TravelDetailUiState, 
    onProfileClick: () -> Unit,
    statsActions: @Composable (RowScope.() -> Unit)? = null
) {
    val travel = uiState.travel
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        UserHeader(
            user = uiState.creatorUser,
            avatarSize = 48,
            showBadges = true,
            onClick = onProfileClick,
            badges = {
                Badge(containerColor = MaterialTheme.colorScheme.surface) {
                    Text(
                        text = travel?.visibility?.name ?: "PRIVATE",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
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
            statsActions?.invoke(this)
        }
    }
}
