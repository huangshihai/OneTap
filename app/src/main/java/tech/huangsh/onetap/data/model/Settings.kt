package tech.huangsh.onetap.data.model

/**
 * 设置项数据模型
 */
data class Settings(
    val voiceEnabled: Boolean = true,
    val voiceSpeed: Float = 1.0f,
    val voiceVolume: Float = 0.8f,
    val fontSize: FontSize = FontSize.MEDIUM,
    val contrastMode: ContrastMode = ContrastMode.NORMAL,
    val themeMode: ThemeMode = ThemeMode.BLUE,
    val launcherMode: Boolean = true,
    val floatingBallEnabled: Boolean = true,
    val autoStartEnabled: Boolean = true,
    val password: String = "123456", // 默认密码
    val isDefaultLauncher: Boolean = false, // 是否为默认桌面
    val showExitLauncher: Boolean = true, // 是否显示退出桌面功能
    val launcherExitConfirmation: Boolean = true, // 退出桌面时是否需要确认
) {
    // 以下属性是为了方便UI使用而添加的计算属性
    val voiceAssistantEnabled: Boolean get() = voiceEnabled
    val voiceFeedbackEnabled: Boolean get() = voiceEnabled
    val highContrast: Boolean get() = contrastMode == ContrastMode.HIGH
}

/**
 * 字体大小枚举
 */
enum class FontSize {
    SMALL,
    MEDIUM,
    LARGE;

    val intValue: Int
        get() = when (this) {
            SMALL -> 0
            MEDIUM -> 1
            LARGE -> 2
        }
}

/**
 * 对比度模式枚举
 */
enum class ContrastMode {
    NORMAL,
    HIGH
}

/**
 * 主题模式枚举
 */
enum class ThemeMode {
    BLUE,
    ORANGE
}

/**
 * 应用分类枚举
 */
enum class AppCategory {
    SOCIAL,
    TOOLS,
    ENTERTAINMENT,
    LIFE,
    OTHER
}

/**
 * 联系人操作类型
 */
enum class ContactAction {
    VIDEO_CALL,
    VOICE_CALL,
    PHONE_CALL
}