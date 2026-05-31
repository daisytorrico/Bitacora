package com.catedra.bitacora.core.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

object LocationPermissionUtils {
    val locationPermissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    fun hasLocationPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }
}

@Composable
fun LocationPermissionHandler(
    onPermissionsResult: (Boolean) -> Unit = {}
) {
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val isGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
                || permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        
        if (!isGranted) {
            Toast.makeText(
                context,
                "Se requieren permisos de ubicación para la funcionalidad completa",
                Toast.LENGTH_LONG
            ).show()
        }
        onPermissionsResult(isGranted)
    }

    LaunchedEffect(Unit) {
        if (!LocationPermissionUtils.hasLocationPermission(context)) {
            permissionLauncher.launch(LocationPermissionUtils.locationPermissions)
        } else {
            onPermissionsResult(true)
        }
    }
}
