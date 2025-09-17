package tech.huangsh.onetap.ui.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import tech.huangsh.onetap.ui.screens.settings.SettingsScreen
import dagger.hilt.android.AndroidEntryPoint
import tech.huangsh.onetap.ui.theme.OneTapTheme
import tech.huangsh.onetap.viewmodel.SettingsViewModel
import tech.huangsh.onetap.data.model.Settings

@AndroidEntryPoint
class SettingsActivity : ComponentActivity() {
    
    private val viewModel: SettingsViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            val settings by viewModel.settings.collectAsState(initial = Settings())
            OneTapTheme(
                darkTheme = false,
                highContrast = settings.highContrast,
                fontSize = settings.fontSize,
                themeMode = settings.themeMode
            ) {
                SettingsScreen(
                    onBack = { finish() },
                    settingsViewModel = viewModel
                )
            }
        }
    }
}