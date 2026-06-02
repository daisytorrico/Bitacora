package com.catedra.bitacora

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.catedra.bitacora.features.auth.presentation.AuthViewModel
import com.catedra.bitacora.core.ui.theme.BitacoraTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val isDark = (resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK) == android.content.res.Configuration.UI_MODE_NIGHT_YES
        val surfaceColor = if (isDark) android.graphics.Color.rgb(15, 15, 38) else android.graphics.Color.WHITE
        
        enableEdgeToEdge(
            navigationBarStyle = SystemBarStyle.auto(surfaceColor, surfaceColor)
        )
        setContent {
            BitacoraTheme {
                AppNavigation(viewModel = authViewModel)
            }
        }
    }
}
