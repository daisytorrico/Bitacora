package com.catedra.bitacora

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalConfiguration
import androidx.core.content.ContextCompat
import com.catedra.bitacora.core.ui.theme.BitacoraTheme
import com.catedra.bitacora.features.auth.presentation.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val authViewModel: AuthViewModel by viewModels()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestNotificationPermissionIfNeeded()

        val isDark = (resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK) == android.content.res.Configuration.UI_MODE_NIGHT_YES
        val surfaceColor = if (isDark) android.graphics.Color.rgb(15, 15, 38) else android.graphics.Color.WHITE

        enableEdgeToEdge(
            navigationBarStyle = SystemBarStyle.auto(surfaceColor, surfaceColor)
        )
        setContent {
            val locales = AppCompatDelegate.getApplicationLocales()
            val configuration = LocalConfiguration.current
            if (!locales.isEmpty) {
                locales.get(0)?.let { configuration.setLocale(it) }
            }
            
            CompositionLocalProvider(LocalConfiguration provides configuration) {
                BitacoraTheme {
                    AppNavigation(viewModel = authViewModel)
                }
            }
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!granted) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}