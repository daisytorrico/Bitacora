package com.catedra.bitacora.core.ui.components.travel

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.catedra.bitacora.features.travel.presentation.travelList.TravelListUiState
import com.catedra.bitacora.core.ui.components.profile.ProfileHeader

@Composable
fun TravelListContent(
    uiState: TravelListUiState,
    onTravelClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    onEditarPerfilClick: (() -> Unit)? = null,
    paddingValues: PaddingValues = PaddingValues(0.dp),
    headerActions: @Composable (RowScope.() -> Unit)? = null,
    middleContent: (@Composable () -> Unit)? = null
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            ProfileHeader(
                user = uiState.user,
                travelCount = uiState.myTravels.size,
                onEditClick = onEditarPerfilClick,
                actions = headerActions
            )
        }

        middleContent?.let {
            item { it() }
        }

        if (uiState.filteredMyTravels.isNotEmpty()) {
            items(
                items = uiState.filteredMyTravels,
                key = { it.id }
            ) { travel ->
                TravelItem(
                    travel = travel,
                    pointsCount = travel.pointsCount,
                    onClick = { onTravelClick(travel.id) }
                )
            }
        }

        if (uiState.filteredSharedTravels.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Compartidos conmigo",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            items(
                items = uiState.filteredSharedTravels,
                key = { "shared_${it.id}" }
            ) { travel ->
                TravelItem(
                    travel = travel,
                    pointsCount = travel.pointsCount,
                    onClick = { onTravelClick(travel.id) }
                )
            }
        }
    }
}