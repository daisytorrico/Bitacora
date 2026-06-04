package com.catedra.bitacora.features.travel.presentation.travelEdit

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.catedra.bitacora.core.ui.components.common.AppTopBar
import com.catedra.bitacora.core.ui.components.form.TravelFormContent
import com.catedra.bitacora.features.travel.presentation.travelCreate.CreateTravelViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTravelScreen(
    onBack: () -> Unit,
    onTravelUpdated: () -> Unit,
    viewModel: EditTravelViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.success) {
        if (uiState.success) {
            onTravelUpdated()
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.resetError()
        }
    }

    // Usamos el CreateTravelViewModel temporalmente para las utilidades de imagen si es necesario, 
    // o movemos las utilidades a un sitio común. Por ahora, simplificamos.
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
                titulo = "Editar Viaje",
                onBack = onBack,
                actions = {
                    IconButton(
                        onClick = { if (uiState.canSave) viewModel.setShowConfirmDialog(true) },
                        enabled = uiState.canSave && !uiState.isLoading
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Guardar")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.isInitialLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
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
                        imageUrl = uiState.imageUrl,
                        onClickAddFoto = {
                            val permissionCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                            if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                                viewModel.buildSystemChooserIntent().let { selectorLauncher.launch(it) }
                            } else {
                                permissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        },
                        isDateInvalid = uiState.isDateInvalid,
                        isLoading = uiState.isLoading
                    )

                    if (uiState.isLoading) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }
                }

                if (uiState.showConfirmDialog) {
                    AlertDialog(
                        onDismissRequest = { viewModel.setShowConfirmDialog(false) },
                        title = { Text("Confirmar cambios") },
                        text = { Text("¿Estás seguro de que deseas guardar los cambios realizados en este viaje?") },
                        confirmButton = {
                            TextButton(onClick = { viewModel.updateTravel() }) {
                                Text("Guardar")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { viewModel.setShowConfirmDialog(false) }) {
                                Text("Cancelar")
                            }
                        }
                    )
                }
            }
        }
    }
}
