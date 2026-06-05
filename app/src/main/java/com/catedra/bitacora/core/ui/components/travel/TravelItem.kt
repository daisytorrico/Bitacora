package com.catedra.bitacora.core.ui.components.travel

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.rounded.ArrowOutward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.catedra.bitacora.features.travel.domain.model.Travel
import com.catedra.bitacora.features.travel.domain.model.TravelStatus
import com.catedra.bitacora.features.travel.domain.model.TravelVisibility
import com.catedra.bitacora.core.ui.theme.GrisMedio
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

@Composable
fun TravelItem(
    travel: Travel,
    pointsCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateRange = remember(travel) { getDateRange(travel) }
    val updateText = remember(travel.updatedAt) { getRelativeUpdateText(travel.updatedAt) }
    val isDark = !MaterialTheme.colorScheme.surface.isLight()
    
    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary
    val outline = MaterialTheme.colorScheme.outline
    val cardBg = MaterialTheme.colorScheme.surface

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(155.dp)
            .padding(vertical = 4.dp, horizontal = 2.dp)
    ) {
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp)
                .shadow(
                    elevation = 16.dp,
                    shape = RoundedCornerShape(32.dp),
                    spotColor = primary,
                    ambientColor = secondary.copy(alpha = 0.4f)
                )
        )

        // Tarjeta de viaje
        Card(
            modifier = Modifier
                .fillMaxSize()
                .clickable { onClick() },
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = cardBg),
            border = BorderStroke(
                width = 1.2.dp,
                brush = Brush.linearGradient(
                    listOf(primary.copy(alpha = 0.4f), Color.Transparent)
                )
            )
        ) {
            Box(modifier = Modifier.fillMaxSize().drawBehind {
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(primary.copy(alpha = 0.03f), Color.Transparent)
                    ),
                    radius = size.maxDimension,
                    center = center.copy(x = size.width * 0.8f)
                )
            }) {
                Row(modifier = Modifier.fillMaxSize()) {
                    AsyncImage(
                        model = travel.imageUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(145.dp)
                            .clip(RoundedCornerShape(topStart = 32.dp, bottomStart = 32.dp)),
                        contentScale = ContentScale.Crop
                    )

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(start = 14.dp, end = 20.dp, top = 22.dp, bottom = 18.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            // titulo
                            Text(
                                text = travel.name,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = if (isDark) Color.White else primary
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(end = 12.dp, top = 16.dp)
                            )
                            
                            Spacer(modifier = Modifier.height(2.dp))
                            
                            Text(
                                text = dateRange.uppercase(),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = outline
                                )
                            )
                        }

                        // Puntos
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Place, null, Modifier.size(13.dp), primary)
                            Spacer(modifier = Modifier.width(5.dp))
                            val puntosLabel = if (pointsCount == 1) "1 PUNTO" else "$pointsCount PUNTOS"
                            Text(
                                text = puntosLabel,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Black,
                                    color = primary
                                )
                            )

                            if (updateText != null) {
                                Text(
                                    text = " • ",
                                    color = outline,
                                    style = MaterialTheme.typography.labelSmall
                                )
                                Text(
                                    text = "ACT. $updateText",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = outline
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // indicadores
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 16.dp, end = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Visibilidad
            Icon(
                imageVector = when (travel.visibility) {
                    TravelVisibility.PRIVATE -> Icons.Outlined.Lock
                    TravelVisibility.FOLLOWERS -> Icons.Outlined.People
                    else -> Icons.Outlined.Public
                },
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = primary.copy(alpha = 0.5f)
            )
            
            StatusCleanBadge(status = travel.status)
        }
        
        // Acceso
        Icon(
            imageVector = Icons.Rounded.ArrowOutward,
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .size(18.dp),
            tint = primary.copy(alpha = 0.2f)
        )
    }
}

@Composable
private fun StatusCleanBadge(status: TravelStatus) {
    val color = when (status) {
        TravelStatus.ONGOING -> Color(0xFF2E7D32)
        TravelStatus.PLANNED -> MaterialTheme.colorScheme.primary
        TravelStatus.COMPLETED -> GrisMedio
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(color, CircleShape)
        )
        Text(
            text = status.label.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Black,
                color = color
            )
        )
    }
}

private fun getRelativeUpdateText(dateTime: LocalDateTime?): String? {
    if (dateTime == null) return null
    val now = LocalDateTime.now()
    val minutes = ChronoUnit.MINUTES.between(dateTime, now)
    return when {
        minutes < 1 -> "AHORA"
        minutes < 60 -> "${minutes}MIN"
        minutes < 1440 -> "${minutes / 60}H"
        else -> "${minutes / 1440}D"
    }
}

private fun getDateRange(travel: Travel): String {
    val start = travel.startDate ?: return "TBD"
    val end = travel.endDate ?: return start.format(DateTimeFormatter.ofPattern("d MMM yyyy", Locale.forLanguageTag("es")))
    val fmt = DateTimeFormatter.ofPattern("d MMM", Locale.forLanguageTag("es"))
    val fmtEnd = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.forLanguageTag("es"))
    return "${start.format(fmt)} — ${end.format(fmtEnd)}"
}

private fun Color.isLight(): Boolean {
    val luminance = 0.299 * red + 0.587 * green + 0.114 * blue
    return luminance > 0.5
}
