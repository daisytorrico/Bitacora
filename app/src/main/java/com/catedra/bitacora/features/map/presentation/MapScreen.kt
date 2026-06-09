package com.catedra.bitacora.features.map.presentation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.catedra.bitacora.R
import com.catedra.bitacora.core.ui.components.map.MapComponent
import com.catedra.bitacora.core.domain.model.ExternalPoi
import com.catedra.bitacora.core.ui.components.common.AppTopBar

@Composable
fun MapScreen(
    navController: NavController,
    onLogout: () -> Unit,
    onNavigateToPoi: (String, String) -> Unit,
    viewModel: MapScreenViewModel = hiltViewModel()
) {
    Scaffold(
        topBar = {
            AppTopBar(
                titulo = stringResource(R.string.map_close_you),
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = stringResource(R.string.log_out)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        MapComponent(
            modifier = Modifier.padding(paddingValues),
            externalPois = viewModel.uiState,
            onExternalPoiSelected = { point ->
                if (point is ExternalPoi) {
                    onNavigateToPoi(point.travelId, point.id)
                }
            },
            externalPoiButtonText = stringResource(R.string.details),
            onCameraMoved = viewModel::onCameraMoved
        )
    }
}
