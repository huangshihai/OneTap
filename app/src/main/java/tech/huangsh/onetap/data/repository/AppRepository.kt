package tech.huangsh.onetap.data.repository

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.PermissionInfo
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import tech.huangsh.onetap.data.local.dao.AppInfoDao
import tech.huangsh.onetap.data.model.AppInfo
import tech.huangsh.onetap.utils.PerformanceUtils

/**
 * 应用信息数据仓库
 */
class AppRepository(
    private val appInfoDao: AppInfoDao,
    private val context: Context
) {
    private val packageManager: PackageManager = context.packageManager

    val enabledApps: Flow<List<AppInfo>> = appInfoDao.getEnabledApps()
    val allApps: Flow<List<AppInfo>> = appInfoDao.getAllApps()
    fun getEnabledAppCount(): Flow<Int> = appInfoDao.getEnabledAppCount()

    suspend fun getAppByPackage(packageName: String): AppInfo? =
        appInfoDao.getAppByPackage(packageName)

    suspend fun insertApp(appInfo: AppInfo) = appInfoDao.insertApp(appInfo)

    suspend fun updateApp(appInfo: AppInfo) = appInfoDao.updateApp(appInfo)

    suspend fun deleteApp(appInfo: AppInfo) = appInfoDao.deleteApp(appInfo)

    suspend fun updateAppEnabledStatus(packageName: String, isEnabled: Boolean) {
        appInfoDao.updateAppEnabledStatus(packageName, isEnabled)
    }

    suspend fun disableApp(packageName: String) {
        appInfoDao.updateAppEnabledStatus(packageName, false)
    }

    suspend fun getMaxEnabledAppOrder(): Int? = appInfoDao.getMaxEnabledAppOrder()

    suspend fun getAvailableApps(): List<AppInfo> {
        // 先从数据库获取已缓存的应用
        var cachedApps = appInfoDao.getAllAppsList()
        
        // 如果数据库为空，则执行一次扫描
        if (cachedApps.isEmpty()) {
            scanInstalledApps()
            cachedApps = appInfoDao.getAllAppsList()
        }
        
        return cachedApps
    }

    suspend fun getCachedApps(): List<AppInfo> {
        return appInfoDao.getAllAppsList()
    }

    suspend fun moveApp(packageName: String, fromPosition: Int, toPosition: Int) {
        if (fromPosition == toPosition) return

        // 简化的移动逻辑
        val app = appInfoDao.getAppByPackage(packageName) ?: return
        appInfoDao.updateAppOrder(packageName, toPosition)
    }

    /**
     * 扫描并同步已安装应用
     */
    suspend fun scanInstalledApps() {
        val installedApps = mutableListOf<AppInfo>()
        
        // 使用getInstalledApplications获取所有已安装应用，包含更多标志
        val applicationInfos = packageManager.getInstalledApplications(
            PackageManager.GET_META_DATA or PackageManager.MATCH_DISABLED_COMPONENTS
        )
        val maxOrder = appInfoDao.getMaxOrder() ?: -1

        applicationInfos.forEachIndexed { index, applicationInfo ->
            val packageName = applicationInfo.packageName
            
            // 优化过滤逻辑：减少对系统应用的过度过滤
            if (!shouldIncludeApp(packageName)) {
                return@forEachIndexed
            }
            
            // 获取应用的启动Intent - 放宽限制，不再强制要求启动Intent
            val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
            val isSystemApp = (applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
            
            // 只过滤掉明确不需要的系统应用
            if (launchIntent == null && isSystemApp && !isImportantSystemApp(packageName)) {
                return@forEachIndexed
            }
            
            val appName = applicationInfo.loadLabel(packageManager).toString()
            val icon = applicationInfo.loadIcon(packageManager)
            val iconBytes = drawableToByteArray(icon)

            val packageInfo = try {
                packageManager.getPackageInfo(packageName, 0)
            } catch (e: PackageManager.NameNotFoundException) {
                null
            }

            val installTime = packageInfo?.firstInstallTime ?: 0L
            val lastUpdateTime = packageInfo?.lastUpdateTime ?: 0L

            installedApps.add(
                AppInfo(
                    packageName = packageName,
                    appName = appName,
                    iconBytes = iconBytes,
                    isEnabled = isDefaultEnabledApp(packageName),
                    order = maxOrder + index + 1,
                    installTime = installTime,
                    lastUpdateTime = lastUpdateTime
                )
            )
        }

        // 批量插入
        installedApps.forEach { app ->
            appInfoDao.insertApp(app)
        }
    }

    /**
     * 启动应用
     */
    fun launchApp(packageName: String): Intent? {
        return try {
            packageManager.getLaunchIntentForPackage(packageName)?.apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Drawable转ByteArray（优化版本）
     */
    private fun drawableToByteArray(drawable: Drawable): ByteArray {
        val bitmap = createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.RGB_565)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)

        // 使用压缩后的图片数据
        return PerformanceUtils.compressBitmap(bitmap, 70) // 70%质量
    }

    /**
     * 判断应用是否应该被包含在扫描结果中
     * 修改为显示更多应用，包括大部分用户可用的应用
     */
    private fun shouldIncludeApp(packageName: String): Boolean {
        return try {
            val packageInfo = packageManager.getPackageInfo(packageName, 0)
            val applicationInfo = packageInfo.applicationInfo

            // 判断是否为系统应用
            val isSystemApp = (applicationInfo?.flags?.and(ApplicationInfo.FLAG_SYSTEM)) != 0

            when {
                // 用户安装的应用全部包含
                !isSystemApp -> true
                
                // 系统应用：更宽松的包含策略
                isSystemApp -> {
                    // 排除明确不需要的系统组件
                    !isExcludedSystemApp(packageName) && (
                        isImportantSystemApp(packageName) || 
                        isUsefulSystemApp(packageName) ||
                        hasLaunchIntent(packageName) ||
                        isUserVisibleApp(packageName)
                    )
                }
                
                else -> false
            }
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    /**
     * 判断是否为有用的系统应用（扩展列表）
     */
    private fun isUsefulSystemApp(packageName: String): Boolean {
        val usefulSystemApps = listOf(
            // 系统工具类
            "com.android.settings",          // 设置
            "com.android.systemui",          // 系统UI
            "com.android.providers.settings", // 设置提供者
            "com.android.packageinstaller",  // 包安装器
            
            // 媒体相关
            "com.android.providers.media",   // 媒体提供者
            "com.android.providers.downloads", // 下载提供者
            
            // 通讯相关
            "com.android.providers.contacts", // 联系人提供者
            "com.android.providers.telephony", // 电话提供者
            
            // 输入法
            "com.android.inputmethod.latin", // AOSP输入法
            "com.sohu.inputmethod.sogou",   // 搜狗输入法
            "com.baidu.input",               // 百度输入法
            
            // 应用商店
            "com.android.vending",           // Google Play商店
            "com.huawei.appmarket",         // 华为应用市场
            "com.xiaomi.market",            // 小米应用商店
            "com.bbk.appstore",             // vivo应用商店
            "com.oppo.market",              // oppo应用商店
            
            // 浏览器
            "com.android.browser",           // 系统浏览器
            "com.chrome.beta",               // Chrome测试版
            "com.chrome.dev",                // Chrome开发版
            "com.chrome.canary",             // Chrome金丝雀版
            "com.google.android.chrome",     // Chrome稳定版
            
            // 办公应用
            "com.google.android.apps.docs",  // Google文档
            "com.google.android.apps.sheets", // Google表格
            "com.google.android.apps.slides", // Google幻灯片
            
            // 其他常用系统应用
            "com.google.android.gms",        // Google服务框架
            "com.google.android.gsf",        // Google服务框架
            "com.android.wallpapercropper",  // 壁纸裁剪
            "com.android.wallpaper.livepicker", // 动态壁纸选择器
        )

        return usefulSystemApps.contains(packageName)
    }

    /**
     * 检查应用是否有启动Intent
     */
    private fun hasLaunchIntent(packageName: String): Boolean {
        return try {
            packageManager.getLaunchIntentForPackage(packageName) != null
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 判断是否为重要的系统应用
     * 扩展重要系统应用列表
     */
    private fun isImportantSystemApp(packageName: String): Boolean {
        val importantSystemApps = listOf(
            // 通讯类
            "com.android.phone",              // 电话
            "com.android.dialer",            // 拨号器
            "com.android.contacts",          // 联系人
            "com.android.messaging",         // 短信
            "com.android.mms",               // 彩信
            "com.android.server.telecom",    // 电话服务

            // 相机和媒体
            "com.android.camera",            // 相机
            "com.android.camera2",           // 相机2
            "com.google.android.GoogleCamera", // Google相机
            "com.android.gallery",           // 相册
            "com.android.music",             // 音乐播放器

            // 工具类
            "com.android.calculator2",       // 计算器
            "com.android.calendar",          // 日历
            "com.android.deskclock",         // 时钟
            "com.android.settings",          // 设置
            "com.android.providers.downloads.ui", // 下载管理器

            // 文件管理
            "com.android.documentsui",       // 文件管理器

            // 浏览器
            "com.android.browser",           // 浏览器
            "com.google.android.chrome",     // Chrome浏览器

            // 录音
            "com.android.soundrecorder",     // 录音机

            // 地图和导航
            "com.google.android.apps.maps",  // Google地图
            "com.autonavi.minimap",          // 高德地图
            "com.baidu.BaiduMap",            // 百度地图

            // 邮件
            "com.android.email",             // 邮件
            "com.google.android.gm",         // Gmail

            // 天气
            "com.google.android.apps.weather", // Google天气
            "com.miui.weather",              // 小米天气
            "com.huawei.weather",            // 华为天气

            // 备忘录和笔记
            "com.google.android.keep",       // Google Keep
            "com.miui.notes",                // 小米便签
            "com.huawei.notepad",            // 华为备忘录

            // 健康和运动
            "com.google.android.apps.fitness", // Google Fit
            "com.huawei.health",             // 华为运动健康
            "com.xiaomi.wearable",          // 小米运动健康

            // 安全和清理
            "com.android.settings",          // 设置（包含存储空间清理）
            "com.google.android.apps.meetings", // Google Meet

            // 其他常用应用
            "com.android.vending",           // Google Play商店
            "com.google.android.gms",        // Google服务框架
            "com.google.android.gsf",        // Google服务框架
            "com.android.systemui",          // 系统UI
        )

        return importantSystemApps.contains(packageName)
    }

    /**
     * 默认启用的应用
     */
    private fun isDefaultEnabledApp(packageName: String): Boolean {
        val defaultApps = listOf(
            // 通讯类
            "com.tencent.mm",                // 微信
            "com.ss.android.ugc.aweme"      // 抖音
        )
        return defaultApps.contains(packageName)
    }

    suspend fun getAppCount(): Int = appInfoDao.getAppCount().first()

    suspend fun deleteAllApps() {
        appInfoDao.deleteAllApps()
    }

    /**
     * 判断是否为用户可见的应用
     * 通过检查应用是否有图标、标签等用户界面元素
     */
    private fun isUserVisibleApp(packageName: String): Boolean {
        return try {
            val applicationInfo = packageManager.getApplicationInfo(packageName, 0)
            val appName = applicationInfo.loadLabel(packageManager).toString()
            val icon = applicationInfo.loadIcon(packageManager)
            
            // 有意义的应用名称和图标
            appName.isNotBlank() && 
            appName != packageName && 
            icon != null &&
            !packageName.contains("test") &&
            !packageName.contains("stub") &&
            !packageName.endsWith(".provider") &&
            !packageName.endsWith(".service")
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 明确排除的系统组件列表
     * 这些是纯后台服务或开发者工具，用户不需要看到
     */
    private fun isExcludedSystemApp(packageName: String): Boolean {
        val excludedApps = listOf(
            // 系统服务和提供者
            "android",
            "com.android.shell",
            "com.android.sharedstoragebackup",
            "com.android.printspooler",
            "com.android.externalstorage",
            "com.android.providers.partnerbookmarks",
            "com.android.proxyhandler",
            "com.android.fallback",
            "com.android.managedprovisioning",
            "com.android.defcontainer",
            "com.android.backupconfirm",
            "com.android.keychain",
            "com.android.pacprocessor",
            "com.android.statementservice",
            "com.android.server.telecom",
            
            // 测试和调试相关
            "com.android.cts",
            "com.android.development",
            "com.android.smoketest",
            "com.android.test",
            "com.android.emulator",
            
            // 隐藏的系统UI组件
            "com.android.systemui.tests",
            "com.android.companiondevicemanager",
            "com.android.bips", // 内置打印服务
            "com.android.bluetoothmidiservice",
            "com.android.bookmarkprovider",
            "com.android.calllogbackup",
            "com.android.captiveportallogin",
            "com.android.cellbroadcastreceiver",
            "com.android.certinstaller",
            "com.android.companiondevicemanager",
            "com.android.dreams.basic",
            "com.android.dreams.phototable",
            "com.android.emergency",
            "com.android.htmlviewer",
            "com.android.inputdevices",
            "com.android.location.fused",
            "com.android.managedprovisioning",
            "com.android.nfc",
            "com.android.onetimeinitializer",
            "com.android.providers.userdictionary",
            "com.android.vpndialogs",
            "com.android.wallpaperbackup",
            "com.android.webview",
        )
        
        return excludedApps.any { excluded ->
            packageName == excluded || packageName.startsWith("$excluded.")
        }
    }

    /**
     * 检查是否有获取应用列表权限
     */
    fun hasQueryAllPackagesPermission(): Boolean {
        return if (isMiuiSystem()) {
            // 小米系统：检查小米专有权限
            hasMiuiGetInstalledAppsPermission()
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // 其他系统Android 11+：检查标准权限
            ContextCompat.checkSelfPermission(context, android.Manifest.permission.QUERY_ALL_PACKAGES) == PackageManager.PERMISSION_GRANTED
        } else {
            // Android 11以下版本不需要此权限
            true
        }
    }

    /**
     * 检查小米系统的获取应用列表权限
     */
    private fun hasMiuiGetInstalledAppsPermission(): Boolean {
        return ContextCompat.checkSelfPermission(context, "com.android.permission.GET_INSTALLED_APPS") == PackageManager.PERMISSION_GRANTED
    }

    /**
     * 检查小米系统是否支持动态申请获取应用列表权限
     */
    fun isMiuiSupportDynamicPermission(): Boolean {
        return try {
            val permissionInfo = packageManager.getPermissionInfo("com.android.permission.GET_INSTALLED_APPS", 0)
            permissionInfo != null && permissionInfo.packageName == "com.lbe.security.miui"
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    /**
     * 检查是否为小米系统
     */
    fun isMiuiSystem(): Boolean {
        return Build.MANUFACTURER.equals("Xiaomi", ignoreCase = true) ||
               Build.BRAND.equals("Xiaomi", ignoreCase = true) ||
               Build.BRAND.equals("Redmi", ignoreCase = true) ||
               getSystemProperty("ro.miui.ui.version.name").isNotEmpty()
    }

    /**
     * 获取系统属性
     */
    private fun getSystemProperty(key: String): String {
        return try {
            val systemProperties = Class.forName("android.os.SystemProperties")
            val get = systemProperties.getMethod("get", String::class.java)
            get.invoke(null, key) as? String ?: ""
        } catch (e: Exception) {
            ""
        }
    }

    /**
     * 检查是否需要动态申请权限
     */
    fun needsDynamicPermissionRequest(): Boolean {
        return if (isMiuiSystem()) {
            // 小米系统：检查是否支持动态申请且没有权限
            isMiuiSupportDynamicPermission() && !hasMiuiGetInstalledAppsPermission()
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // 其他系统：检查标准权限
            ContextCompat.checkSelfPermission(context, android.Manifest.permission.QUERY_ALL_PACKAGES) != PackageManager.PERMISSION_GRANTED
        } else {
            false
        }
    }

    /**
     * 检查小米系统是否需要手动设置权限
     */
    fun needsManualPermissionSetting(): Boolean {
        return isMiuiSystem() && !isMiuiSupportDynamicPermission() && !hasMiuiGetInstalledAppsPermission()
    }

    /**
     * 获取小米系统的权限设置Intent
     */
    fun getMiuiPermissionIntent(): Intent? {
        return try {
            // 尝试跳转到小米的权限管理页面
            Intent().apply {
                setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.PermissionsEditorActivity")
                putExtra("extra_pkgname", context.packageName)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
        } catch (e: Exception) {
            try {
                // 备用方案：跳转到应用详情页面
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = android.net.Uri.fromParts("package", context.packageName, null)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
            } catch (e2: Exception) {
                null
            }
        }
    }

    /**
     * 获取通用的应用设置Intent
     */
    fun getAppSettingsIntent(): Intent {
        return Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = android.net.Uri.fromParts("package", context.packageName, null)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
    }
}