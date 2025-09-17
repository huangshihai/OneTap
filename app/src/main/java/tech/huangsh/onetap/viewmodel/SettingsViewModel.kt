package tech.huangsh.onetap.viewmodel

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import tech.huangsh.onetap.data.model.ContrastMode
import tech.huangsh.onetap.data.model.FontSize
import tech.huangsh.onetap.data.model.ThemeMode
import tech.huangsh.onetap.data.repository.SettingsRepository
import tech.huangsh.onetap.utils.LauncherUtils
import tech.huangsh.onetap.utils.VoiceAssistant
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {
    
    val settings = settingsRepository.settings
    
    private val _voiceAssistant = MutableStateFlow<VoiceAssistant?>(null)
    val voiceAssistant: StateFlow<VoiceAssistant?> = _voiceAssistant
    
    private val _isLauncherMode = MutableStateFlow(false)
    val isLauncherMode: StateFlow<Boolean> = _isLauncherMode
    
    private val _floatingBallEnabled = MutableStateFlow(false)
    val floatingBallEnabled: StateFlow<Boolean> = _floatingBallEnabled
    
    private val _isDefaultLauncher = MutableStateFlow(false)
    val isDefaultLauncher: StateFlow<Boolean> = _isDefaultLauncher
    
    init {
        checkLauncherMode()
        loadFloatingBallSetting()
        initializeVoiceAssistant()
        checkDefaultLauncherStatus()
    }
    
    /**
     * 检查是否是默认桌面
     */
    private fun checkLauncherMode() {
        viewModelScope.launch {
            val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            _isLauncherMode.value = am.isLowRamDevice // Simplified check
        }
    }
    
    /**
     * 检查默认启动器状态
     */
    private fun checkDefaultLauncherStatus() {
        viewModelScope.launch {
            val isDefault = LauncherUtils.isDefaultLauncher(context)
            _isDefaultLauncher.value = isDefault
            settingsRepository.updateIsDefaultLauncher(isDefault)
        }
    }
    
    /**
     * 加载悬浮球设置
     */
    private fun loadFloatingBallSetting() {
        viewModelScope.launch {
            val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
            _floatingBallEnabled.value = prefs.getBoolean("floating_ball_enabled", false)
        }
    }
    
    /**
     * 初始化语音助手
     */
    private fun initializeVoiceAssistant() {
        _voiceAssistant.value = VoiceAssistant(context)
    }
    
    /**
     * 更新语音辅助开关
     */
    fun updateVoiceAssistantEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateVoiceEnabled(enabled)
        }
    }
    
    /**
     * 更新语音反馈开关
     */
    fun updateVoiceFeedbackEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateVoiceEnabled(enabled)
        }
    }
    
    /**
     * 更新语音速度
     */
    fun updateVoiceSpeed(speed: Float) {
        viewModelScope.launch {
            settingsRepository.updateVoiceSpeed(speed)
        }
    }
    
    /**
     * 更新语音音量
     */
    fun updateVoiceVolume(volume: Float) {
        viewModelScope.launch {
            settingsRepository.updateVoiceVolume(volume)
        }
    }
    
    /**
     * 更新字体大小
     */
    fun updateFontSize(size: FontSize) {
        viewModelScope.launch {
            settingsRepository.updateFontSize(size)
        }
    }
    
    /**
     * 更新对比度
     */
    fun updateContrastMode(highContrast: Boolean) {
        viewModelScope.launch {
            val mode = if (highContrast) ContrastMode.HIGH else ContrastMode.NORMAL
            settingsRepository.updateContrastMode(mode)
        }
    }
    
    /**
     * 更新主题模式
     */
    fun updateThemeMode(themeMode: ThemeMode) {
        viewModelScope.launch {
            settingsRepository.updateThemeMode(themeMode)
        }
    }
    
    /**
     * 更新开机自启动
     */
    fun updateAutoStartEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateAutoStartEnabled(enabled)
        }
    }
    
    /**
     * 测试语音
     */
    fun testVoice() {
        voiceAssistant.value?.let { assistant ->
            assistant.speak("您好，我是一键通，很高兴为您服务！")
        }
    }
    
    /**
     * 退出到系统桌面
     */
    fun exitToSystemDesktop() {
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }
    
    /**
     * 退出桌面启动器模式
     */
    fun exitLauncherMode() {
        LauncherUtils.exitLauncherMode(context)
    }
    
    /**
     * 打开默认应用设置
     */
    fun openDefaultAppSettings() {
        LauncherUtils.openDefaultAppSettings(context)
    }
    
    /**
     * 触发默认桌面选择器
     */
    fun triggerDefaultLauncherChooser() {
        LauncherUtils.triggerDefaultLauncherChooser(context)
    }
    
    /**
     * 重置为系统默认桌面
     */
    fun resetToSystemLauncher() {
        LauncherUtils.resetToSystemLauncher(context)
    }
    
    /**
     * 更新桌面启动器相关设置
     */
    fun updateShowExitLauncher(show: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateShowExitLauncher(show)
        }
    }
    
    fun updateLauncherExitConfirmation(needConfirmation: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateLauncherExitConfirmation(needConfirmation)
        }
    }
    
    /**
     * 刷新默认启动器状态
     */
    fun refreshDefaultLauncherStatus() {
        checkDefaultLauncherStatus()
    }
    
    /**
     * 朗读界面元素
     */
    suspend fun speakElement(elementName: String) {
        if (settings.first().voiceFeedbackEnabled) {
            voiceAssistant.value?.speak(elementName)
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        voiceAssistant.value?.shutdown()
    }
}