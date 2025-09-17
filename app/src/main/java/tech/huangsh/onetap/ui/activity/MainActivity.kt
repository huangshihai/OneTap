package tech.huangsh.onetap.ui.activity

import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import tech.huangsh.onetap.data.model.Settings
import tech.huangsh.onetap.ui.screens.home.HomeScreen
import tech.huangsh.onetap.ui.theme.OneTapTheme
import tech.huangsh.onetap.utils.LauncherUtils
import tech.huangsh.onetap.viewmodel.MainViewModel
import tech.huangsh.onetap.viewmodel.SettingsViewModel

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // 检查并更新默认启动器状态
        updateLauncherStatus()
        
        setContent {
            val settings by settingsViewModel.settings.collectAsState(initial = Settings())
            OneTapTheme(
                darkTheme = false,
                highContrast = settings.highContrast,
                fontSize = settings.fontSize,
                themeMode = settings.themeMode
            ) {
                HomeScreen(viewModel)
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        // 每次恢复时检查启动器状态
        updateLauncherStatus()
    }
    
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        // 处理Home键和返回键，确保启动器行为正确
        return when (keyCode) {
            KeyEvent.KEYCODE_HOME -> {
                // 如果是默认启动器，不做任何处理，保持在当前界面
                if (LauncherUtils.isDefaultLauncher(this)) {
                    true
                } else {
                    super.onKeyDown(keyCode, event)
                }
            }
            KeyEvent.KEYCODE_BACK -> {
                // 如果是默认启动器，返回键也不退出应用
                if (LauncherUtils.isDefaultLauncher(this)) {
                    true
                } else {
                    super.onKeyDown(keyCode, event)
                }
            }
            else -> super.onKeyDown(keyCode, event)
        }
    }
    
    /**
     * 更新启动器状态
     */
    private fun updateLauncherStatus() {
        lifecycleScope.launch {
            settingsViewModel.refreshDefaultLauncherStatus()
        }
    }
}