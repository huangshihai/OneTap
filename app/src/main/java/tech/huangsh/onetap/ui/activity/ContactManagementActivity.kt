package tech.huangsh.onetap.ui.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import dagger.hilt.android.AndroidEntryPoint
import tech.huangsh.onetap.ui.theme.OneTapTheme
import tech.huangsh.onetap.ui.screens.contact.ContactManagementScreen
import tech.huangsh.onetap.viewmodel.ContactViewModel
import tech.huangsh.onetap.viewmodel.SettingsViewModel
import tech.huangsh.onetap.data.model.Settings

@AndroidEntryPoint
class ContactManagementActivity : ComponentActivity() {
    
    private val viewModel: ContactViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            val settings by settingsViewModel.settings.collectAsState(initial = Settings())
            OneTapTheme(
                darkTheme = false,
                highContrast = settings.highContrast,
                fontSize = settings.fontSize,
                themeMode = settings.themeMode
            ) {
                ContactManagementScreen(
                    onBack = { finish() },
                    onAddContact = {
                        ContactDetailActivity.start(this, null, true)
                    }
                )
            }
        }
    }
}