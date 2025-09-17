package tech.huangsh.onetap.viewmodel

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import tech.huangsh.onetap.data.model.AppInfo
import tech.huangsh.onetap.data.repository.AppRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppViewModel @Inject constructor(
    private val appRepository: AppRepository
) : ViewModel() {
    
    // 已启用的应用
    val enabledApps = appRepository.enabledApps
    
    // 可用的应用列表
    private val _availableApps = MutableStateFlow<List<AppInfo>>(emptyList())
    val availableApps = _availableApps.asStateFlow()
    
    // 加载状态
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()
    
    // 权限状态
    private val _hasPermission = MutableStateFlow(false)
    val hasPermission = _hasPermission.asStateFlow()
    
    /**
     * 加载可用应用
     */
    fun loadAvailableApps() {
        viewModelScope.launch {
            _availableApps.value = appRepository.getAvailableApps()
        }
    }
    
    /**
     * 扫描已安装应用
     */
    fun scanApps() {
        viewModelScope.launch {
            appRepository.scanInstalledApps()
            loadAvailableApps()
        }
    }
    
    /**
     * 刷新应用列表（强制重新扫描）
     */
    fun refreshApps() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // 先清空现有应用列表
                appRepository.deleteAllApps()
                // 重新扫描所有应用
                appRepository.scanInstalledApps()
                // 重新加载可用应用
                loadAvailableApps()
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * 启用应用
     */
    fun enableApp(app: AppInfo) {
        viewModelScope.launch {
            val maxOrder = appRepository.getMaxEnabledAppOrder() ?: -1
            val enabledApp = app.copy(
                isEnabled = true,
                order = maxOrder + 1
            )
            appRepository.insertApp(enabledApp)
        }
    }
    
    /**
     * 禁用应用
     */
    fun disableApp(packageName: String) {
        viewModelScope.launch {
            appRepository.disableApp(packageName)
        }
    }
    
    /**
     * 移动应用位置
     */
    fun moveApp(packageName: String, fromPosition: Int, toPosition: Int) {
        viewModelScope.launch {
            appRepository.moveApp(packageName, fromPosition, toPosition)
        }
    }

    /**
     * 检查权限状态
     */
    fun checkPermission() {
        _hasPermission.value = appRepository.hasQueryAllPackagesPermission()
    }

    /**
     * 检查是否需要动态申请权限
     */
    fun needsDynamicPermissionRequest(): Boolean {
        return appRepository.needsDynamicPermissionRequest()
    }

    /**
     * 检查小米系统是否需要手动设置权限
     */
    fun needsManualPermissionSetting(): Boolean {
        return appRepository.needsManualPermissionSetting()
    }

    /**
     * 检查小米系统是否支持动态权限申请
     */
    fun isMiuiSupportDynamicPermission(): Boolean {
        return appRepository.isMiuiSupportDynamicPermission()
    }

    /**
     * 获取小米系统的权限设置Intent
     */
    fun getMiuiPermissionIntent(): Intent? {
        return appRepository.getMiuiPermissionIntent()
    }

    /**
     * 获取通用的应用设置Intent
     */
    fun getAppSettingsIntent(): Intent {
        return appRepository.getAppSettingsIntent()
    }

}