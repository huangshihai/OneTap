package tech.huangsh.onetap.viewmodel

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import tech.huangsh.onetap.data.model.Contact
import tech.huangsh.onetap.data.model.WeatherInfo
import tech.huangsh.onetap.data.remote.WeatherService
import tech.huangsh.onetap.data.repository.AppRepository
import tech.huangsh.onetap.data.repository.ContactRepository
import tech.huangsh.onetap.utils.DateUtils
import java.util.*
import javax.inject.Inject

/**
 * 主界面ViewModel
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    private val contactRepository: ContactRepository,
    private val appRepository: AppRepository,
    private val weatherService: WeatherService
) : ViewModel() {
    
    // 联系人数据
    private val _contacts = contactRepository.allContacts
    val contacts = _contacts.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )
    
    // 应用数据
    private val _apps = appRepository.enabledApps
    val apps = _apps.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )
    
    // 时间和日期
    private val _currentTime = MutableStateFlow("")
    val currentTime = _currentTime.asStateFlow()
    
    private val _currentDate = MutableStateFlow("")
    val currentDate = _currentDate.asStateFlow()

    private val _currentWeek = MutableStateFlow("")
    val currentWeek = _currentWeek.asStateFlow()

    private val _currentLunarDate = MutableStateFlow("")
    val currentLunarDate = _currentLunarDate.asStateFlow()
    // 天气信息
    private val _weatherInfo = MutableStateFlow<WeatherInfo?>(null)
    val weatherInfo = _weatherInfo.asStateFlow()
    
    // 底部菜单状态
    private val _showBottomSheet = MutableStateFlow(false)
    val showBottomSheet = _showBottomSheet.asStateFlow()
    
    private val _selectedContact = MutableStateFlow<Contact?>(null)
    val selectedContact = _selectedContact.asStateFlow()
    
    init {
        updateTime()
        // 启动时更新天气
        updateWeather()

        // 定时更新天气（每小时）
        viewModelScope.launch {
            while (true) {
                delay(60 * 60 * 1000) // 1小时
                updateWeather()
            }
        }
    }
    
    /**
     * 更新时间
     */
    private fun updateTime() {
        viewModelScope.launch {
            while (true) {
                val now = Date()

                _currentTime.value = DateUtils.formatTime(now)
                _currentDate.value = DateUtils.formatDate(now)
                _currentWeek.value = DateUtils.getWeekDay(now)
                _currentLunarDate.value = DateUtils.getLunar(now)

                delay(1000)
            }
        }
    }
    
    /**
     * 显示联系人操作菜单
     */
    fun showContactActions(contact: Contact) {
        _selectedContact.value = contact
        _showBottomSheet.value = true
    }
    
    /**
     * 隐藏底部菜单
     */
    fun hideBottomSheet() {
        _showBottomSheet.value = false
        _selectedContact.value = null
    }
    
    /**
     * 启动应用
     */
    fun launchApp(packageName: String): Intent? {
        return appRepository.launchApp(packageName)
    }
    
    /**
     * 打电话
     */
    fun makePhoneCall(phoneNumber: String?): Intent? {
        return phoneNumber?.let {
            contactRepository.makePhoneCall(it)
        }
    }
    
    /**
     * 发起微信视频通话
     */
    fun startWeChatVideoCall(wechatNickname: String?) {
        contactRepository.startWeChatVideoCall(wechatNickname)
    }
    
    /**
     * 发起微信语音通话
     */
    fun startWeChatVoiceCall(wechatNickname: String?) {
        contactRepository.startWeChatVoiceCall(wechatNickname)
    }
    
    /**
     * 更新天气信息
     */
    fun updateWeather() {
        viewModelScope.launch {
            try {
                _weatherInfo.value = weatherService.getWeatherInfo()
            } catch (e: Exception) {
                e.printStackTrace()
                // 保持之前的天气信息或使用默认值
                if (_weatherInfo.value == null) {
                    _weatherInfo.value = WeatherInfo(
                        temperature = 25,
                        weather = "晴",
                        weatherIcon = "☀️"
                    )
                }
            }
        }
    }
}