package com.catedra.bitacora.core.ui.components.travel

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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.catedra.bitacora.features.travel.domain.model.Travel
import com.catedra.bitacora.core.ui.theme.GrisMedio
import com.catedra.bitacora.core.ui.theme.GrisSeparador
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@Composable
fun TravelItem(
    travel: Travel,
    pointsCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateRange = remember(travel) { getDateRange(travel) }
    val updateText = remember(travel.updatedAt) { getRelativeUpdateText(travel.updatedAt) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(115.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, GrisSeparador),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = travel.imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .width(110.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp)),
                contentScale = ContentScale.Crop
            )

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
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 13.sp
                        )
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "$pointsCount paradas",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = GrisMedio)
                        )
                        if (updateText != null) {
                            Text(
                                text = " • ",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = GrisMedio)
                            )
                            Text(
                                text = updateText,
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = GrisMedio)
                            )
                        }
                    }
                    
                    Text(
                        text = travel.status.label,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = GrisMedio,
                modifier = Modifier.padding(end = 12.dp)
            )
        }
    }
}

private fun getRelativeUpdateText(dateTime: LocalDateTime?): String? {
    if (dateTime == null) return null
    val now = LocalDateTime.now()
    
    val minutes = ChronoUnit.MINUTES.between(dateTime, now)
    if (minutes < 1) return "Recién"
    if (minutes < 60) return "Hace $minutes m"
    
    val hours = ChronoUnit.HOURS.between(dateTime, now)
    if (hours < 24) return "Hace $hours h"
    
    val days = ChronoUnit.DAYS.between(dateTime.toLocalDate(), now.toLocalDate())
    return when {
        days == 1L -> "Ayer"
        days < 7L -> "Hace $days d"
        else -> dateTime.format(DateTimeFormatter.ofPattern("dd/MM"))
    }
}

private fun getDateRange(travel: Travel): String {
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    val start = travel.startDate?.format(formatter) ?: ""
    val end = travel.endDate?.format(formatter) ?: ""
    
    return if (start.isNotEmpty() && end.isNotEmpty()) {
        "$start - $end"
    } else start.ifEmpty {
        "Sin fechas"
    }
}
