package com.catedra.bitacora.features.travel.presentation.travelCreate

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.catedra.bitacora.features.travel.domain.model.TravelVisibility
import com.catedra.bitacora.core.ui.components.form.TravelFormContent
import com.catedra.bitacora.core.ui.components.common.AppTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTravelScreen(
    onBack: () -> Unit,
    onTravelCreated: (String) -> Unit,
    viewModel: CreateTravelViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    // Manejo de eventos de navegación y errores
    LaunchedEffect(uiState.success, uiState.travelId) {
        val travelId = uiState.travelId
        if (uiState.success && travelId != null) {
            onTravelCreated(travelId)
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.resetError()
        }
    }

    val selectorLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val selectedUri = result.data?.data ?: viewModel.getActiveTempUri()
            viewModel.onImageSelected(selectedUri)
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.buildSystemChooserIntent().let { selectorLauncher.launch(it) }
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                titulo = "Nuevo Viaje",
                onBack = onBack,
                actions = {
                    IconButton(
                        onClick = { viewModel.saveTravel() },
                        enabled = uiState.canSave && !uiState.loading
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Guardar")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                TravelFormContent(
                    name = uiState.name,
                    onNameChange = { viewModel.onNameChange(it) },
                    description = uiState.description,
                    onDescriptionChange = { viewModel.onDescriptionChange(it) },
                    startDate = uiState.startDate,
                    onStartDateSelected = { viewModel.onStartDateSelected(it) },
                    endDate = uiState.endDate,
                    onEndDateSelected = { viewModel.onEndDateSelected(it) },
                    visibility = uiState.visibility,
                    onVisibilityChange = { viewModel.onVisibilityChange(it) },
                    imageUri = uiState.imageUri,
                    imageUrl = null,
                    onClickAddFoto = {
                        val permissionCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                            viewModel.buildSystemChooserIntent().let { selectorLauncher.launch(it) }
                        } else {
                            permissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    },
                    isDateInvalid = uiState.isDateInvalid,
                    isLoading = uiState.loading,
                    modifier = Modifier.weight(1f)
                )

                if (uiState.loading) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
            }
            
            // Overlay de carga
            if (uiState.loading) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Black.copy(alpha = 0.1f)
                ) {}
            }
        }
    }
}
