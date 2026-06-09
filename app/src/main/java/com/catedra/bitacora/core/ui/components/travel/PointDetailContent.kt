package com.catedra.bitacora.core.ui.components.travel

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.catedra.bitacora.R
import com.catedra.bitacora.core.ui.components.map.MapComponent
import com.catedra.bitacora.core.ui.components.profile.UserHeader
import com.catedra.bitacora.core.domain.model.Coordinates
import com.catedra.bitacora.core.domain.model.PointOnMap
import com.catedra.bitacora.features.travel.presentation.pointDetail.PointDetailUiState
import com.catedra.bitacora.core.ui.theme.*
import java.time.format.DateTimeFormatter

@Composable
fun PointDetailContent(
    uiState: PointDetailUiState,
    onProfileClick: () -> Unit,
    onLikeClick: () -> Unit,
    onCommentsClick: () -> Unit,
    onToggleMap: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues = PaddingValues(0.dp),
    actions: @Composable (ColumnScope.() -> Unit)? = null
) {
    val point = uiState.point ?: return

    var showStoriesDialog by remember { mutableStateOf(false) }
    var selectedImageIndex by remember { mutableIntStateOf(0) }

    if (showStoriesDialog && point.imageUrls.isNotEmpty()) {
        PointStoriesDialog(
            imageUrls = point.imageUrls,
            title = point.name,
            initialIndex = selectedImageIndex,
            onDismiss = { showStoriesDialog = false }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(paddingValues)
            .verticalScroll(rememberScrollState())
    ) {
        if (point.imageUrls.isNotEmpty()) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
                    .background(GrisSeparador),
                contentPadding = PaddingValues(horizontal = 0.dp)
            ) {
                items(point.imageUrls.indices.toList()) { index ->
                    AsyncImage(
                        model = point.imageUrls[index],
                        contentDescription = null,
                        modifier = Modifier
                            .fillParentMaxWidth()
                            .height(240.dp)
                            .clickable {
                                selectedImageIndex = index
                                showStoriesDialog = true
                            },
                        contentScale = ContentScale.Crop
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .background(GrisSeparador)
                    .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = GrisMedio
                )
            }
        }

        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            UserHeader(
                user = uiState.creatorUser,
                onClick = onProfileClick
            )

            Spacer(modifier = Modifier.height(16.dp))

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
                        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
                    ) ?: stringResource(R.string.no_date),
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

            Text(
                text = stringResource(R.string.location),
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
                if (point.latitude != null && point.longitude != null) {
                    val pointOnMap = remember(point) {
                        PointOnMap(
                            name = point.name,
                            address = point.address,
                            coordinates = Coordinates(point.latitude, point.longitude)
                        )
                    }
                    MapComponent(
                        modifier = Modifier.fillMaxSize(),
                        initialPoint = pointOnMap,
                        showSearch = false,
                        showControls = false,
                        isInteractive = false,
                        showSelectionCard = false
                    )
                } else {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = "Pin",
                        tint = RojoPin,
                        modifier = Modifier
                            .size(40.dp)
                            .align(Alignment.Center)
                    )
                }
                OutlinedButton(
                    onClick = { onToggleMap(true) },
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = Blanco)
                ) {
                    Text(stringResource(R.string.see_in_map), fontSize = 12.sp)
                }
            }

        Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { onLikeClick() }
                    ) {
                        Icon(
                            imageVector = if (uiState.isLiked) Icons.Filled.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = stringResource(R.string.like),
                            modifier = Modifier.size(24.dp),
                            tint = if (uiState.isLiked) Color.Red else GrisMedio
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = uiState.likesCount.toString(),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (uiState.isLiked) Color.Red else GrisMedio
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { onCommentsClick() }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Comment,
                            contentDescription = stringResource(R.string.comments),
                            modifier = Modifier.size(24.dp),
                            tint = GrisMedio
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = uiState.commentsCount.toString(),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = GrisMedio
                        )
                    }
                }

                IconButton(onClick = { /* Sin lógica por ahora */ }) {
                    Icon(
                        imageVector = Icons.Default.BookmarkBorder,
                        contentDescription = stringResource(R.string.save),
                        modifier = Modifier.size(26.dp),
                        tint = GrisMedio
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.notebook_notes),
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
                    text = point.notes.ifEmpty { stringResource(R.string.no_notes_yet) },
                    modifier = Modifier.padding(16.dp),
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            actions?.invoke(this)

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
