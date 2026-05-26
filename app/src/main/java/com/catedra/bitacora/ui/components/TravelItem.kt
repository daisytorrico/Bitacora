package com.catedra.bitacora.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.catedra.bitacora.features.travel.domain.model.Travel
import com.catedra.bitacora.ui.theme.BitacoraTheme
import com.catedra.bitacora.ui.theme.GrisMedio
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun TravelItem(
    travel: Travel,
    pointsCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val status = remember(travel) { getTravelStatus(travel) }
    val dateRange = remember(travel) { getDateRange(travel) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(115.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE0E0E0)), // Contorno gris claro muy sutil
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Sección Izquierda (Imagen de Portada)
            AsyncImage(
                model = travel.imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .width(110.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp)),
                contentScale = ContentScale.Crop
            )

            // Sección Derecha (Información y Column)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = travel.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.primary
                        ),
                        maxLines = 1
                    )
                    Text(
                        text = dateRange,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = GrisMedio,
                            fontSize = 13.sp
                        )
                    )
                }

                // Metadatos Dinámicos (Fila Inferior)
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$pointsCount puntos",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.sp,
                            color = GrisMedio
                        )
                    )
                    Text(
                        text = " • ",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.sp,
                            color = GrisMedio
                        )
                    )
                    Text(
                        text = status,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    )
                }
            }

            // Navegación e Indicador
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = GrisMedio,
                modifier = Modifier.padding(end = 12.dp)
            )
        }
    }
}

private fun getTravelStatus(travel: Travel): String {
    val today = LocalDate.now()
    val startDate = travel.startDate
    val endDate = travel.endDate

    return when {
        startDate == null || endDate == null -> "Planificando"
        today.isBefore(startDate) -> "Planificando"
        today.isAfter(endDate) -> "Completado"
        else -> "En curso"
    }
}

private fun getDateRange(travel: Travel): String {
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    val start = travel.startDate?.format(formatter) ?: ""
    val end = travel.endDate?.format(formatter) ?: ""
    
    return if (start.isNotEmpty() && end.isNotEmpty()) {
        "$start - $end"
    } else if (start.isNotEmpty()) {
        start
    } else {
        "Sin fechas"
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF4F6F8)
@Composable
fun TravelItemPreview() {
    BitacoraTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            TravelItem(
                travel = Travel(
                    name = "Viaje a Bariloche",
                    startDate = LocalDate.now().minusDays(5),
                    endDate = LocalDate.now().plusDays(5),
                    ownerId = "123"
                ),
                pointsCount = 12,
                onClick = {}
            )
        }
    }
}
