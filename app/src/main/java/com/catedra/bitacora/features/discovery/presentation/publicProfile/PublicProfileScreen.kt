package com.catedra.bitacora.features.discovery.presentation.publicProfile

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.catedra.bitacora.core.ui.components.common.AppTopBar
import com.catedra.bitacora.core.ui.components.travel.TravelListContent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublicProfileScreen(
    viewModel: PublicProfileViewModel,
    onTravelClick: (String) -> Unit,
    navController: NavController
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(uiState.followMessage) {
        uiState.followMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.resetFollowMessage()
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                titulo = uiState.user?.displayName ?: "Perfil",
                onBack = { navController.popBackStack() }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading && uiState.user == null) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            TravelListContent(
                uiState = uiState.toTravelListUiState(),
                onTravelClick = onTravelClick,
                paddingValues = paddingValues,
                headerActions = {
                    IconButton(
                        onClick = { viewModel.toggleFollow() },
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PersonAdd,
                            contentDescription = "Seguir",
                            tint = if (uiState.isFollowing) 
                                MaterialTheme.colorScheme.primary 
                            else 
                                com.catedra.bitacora.core.ui.theme.GrisMedio,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            )
        }
    }
}
