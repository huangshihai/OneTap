package tech.huangsh.onetap.utils

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.provider.Settings
import android.widget.Toast
import tech.huangsh.onetap.ui.activity.LauncherChooserActivity

/**
 * 桌面启动器工具类
 * 处理默认桌面设置和退出功能
 */
object LauncherUtils {

    /**
     * 检查当前应用是否为默认桌面
     */
    fun isDefaultLauncher(context: Context): Boolean {
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
        }
        
        val resolveInfo = context.packageManager.resolveActivity(
            intent, 
            PackageManager.MATCH_DEFAULT_ONLY
        )
        
        return resolveInfo?.activityInfo?.packageName == context.packageName
    }

    /**
     * 获取所有可用的桌面启动器
     */
    fun getAvailableLaunchers(context: Context): List<ResolveInfo> {
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
        }
        
        return context.packageManager.queryIntentActivities(
            intent,
            PackageManager.MATCH_DEFAULT_ONLY or PackageManager.MATCH_ALL
        ).filter { 
            // 过滤掉当前应用
            it.activityInfo.packageName != context.packageName
        }
    }

    /**
     * 打开系统默认应用设置页面，让用户选择默认桌面
     */
    fun openDefaultAppSettings(context: Context) {
        try {
            // 小米MIUI系统特殊处理
            if (isMiuiSystem()) {
                if (tryOpenMiuiDefaultAppSettings(context)) {
                    return
                }
            }
            
            // 尝试打开桌面设置
            val homeIntent = Intent(Settings.ACTION_HOME_SETTINGS)
            homeIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(homeIntent)
        } catch (e: Exception) {
            try {
                // 尝试打开默认应用设置
                val defaultAppsIntent = Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS)
                defaultAppsIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(defaultAppsIntent)
            } catch (e2: Exception) {
                try {
                    // 尝试打开应用信息页面
                    val appInfoIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = android.net.Uri.parse("package:${context.packageName}")
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(appInfoIntent)
                    Toast.makeText(context, "请在应用信息中清除默认设置", Toast.LENGTH_LONG).show()
                } catch (e3: Exception) {
                    Toast.makeText(context, "无法打开设置页面", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    /**
     * 触发默认桌面选择器 - 最简单可靠的方法
     */
    fun triggerDefaultLauncherChooser(context: Context) {
        try {
            // 最直接的方法：创建Home Intent并启动
            val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            
            // 直接启动，让系统决定是否显示选择器
            context.startActivity(homeIntent)
            
            // 给用户一个提示
            Toast.makeText(context, "按Home键或重新打开应用来选择默认桌面", Toast.LENGTH_LONG).show()
            
        } catch (e: Exception) {
            // 如果上面的方法失败，尝试打开设置页面
            try {
                openDefaultAppSettings(context)
            } catch (e2: Exception) {
                Toast.makeText(context, "请手动设置默认桌面：设置 > 应用 > 默认应用 > 桌面", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    /**
     * 强制清除默认桌面设置，触发选择器
     */
    fun forceShowLauncherChooser(context: Context) {
        try {
            // 方法1：使用专门的Activity
            val chooserIntent = Intent(context, LauncherChooserActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(chooserIntent)
            
        } catch (e: Exception) {
            // 方法2：直接启动Home Intent
            triggerDefaultLauncherChooser(context)
        }
    }
    
    /**
     * 尝试打开小米系统的默认应用设置
     */
    private fun tryOpenMiuiDefaultAppSettings(context: Context): Boolean {
        return try {
            // 方法1：尝试打开小米的默认应用设置
            val miuiIntent = Intent().apply {
                action = "miui.intent.action.APP_PERM"
                putExtra("extra_pkgname", context.packageName)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            
            if (isIntentAvailable(context, miuiIntent)) {
                context.startActivity(miuiIntent)
                Toast.makeText(context, "请在\"默认启动\"中取消设置", Toast.LENGTH_LONG).show()
                return true
            }
            
            // 方法2：尝试打开小米的应用管理
            val miuiAppManageIntent = Intent().apply {
                action = "miui.intent.action.OP_AUTO_START"
                putExtra("package_name", context.packageName)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            
            if (isIntentAvailable(context, miuiAppManageIntent)) {
                context.startActivity(miuiAppManageIntent)
                return true
            }
            
            false
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 退出桌面启动器模式，恢复到系统默认桌面
     */
    fun exitLauncherMode(context: Context) {
        try {
            // 方法1：尝试启动系统桌面选择器
            if (tryLaunchSystemChooser(context)) {
                return
            }
            
            // 方法2：尝试直接启动系统默认桌面
            if (tryLaunchSystemLauncher(context)) {
                return
            }
            
            // 方法3：尝试清除默认设置并触发选择器
            if (tryClearDefaultAndChoose(context)) {
                return
            }
            
            // 方法4：引导用户到设置页面
            openDefaultAppSettings(context)
            Toast.makeText(context, "请在设置中选择其他桌面应用", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            // 最后的备用方案
            openDefaultAppSettings(context)
            Toast.makeText(context, "请在设置中选择其他桌面应用", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * 方法1：尝试启动系统桌面选择器
     */
    private fun tryLaunchSystemChooser(context: Context): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            
            // 创建选择器Intent
            val chooser = Intent.createChooser(intent, "选择桌面")
            chooser.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(chooser)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * 方法2：尝试直接启动系统默认桌面
     */
    private fun tryLaunchSystemLauncher(context: Context): Boolean {
        return try {
            val systemLauncher = getSystemDefaultLauncher(context)
            if (systemLauncher != null) {
                val launchers = getAvailableLaunchers(context)
                val targetLauncher = launchers.find { it.activityInfo.packageName == systemLauncher }
                
                if (targetLauncher != null) {
                    launchSpecificLauncher(
                        context,
                        targetLauncher.activityInfo.packageName,
                        targetLauncher.activityInfo.name
                    )
                    return true
                }
            }
            false
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * 方法3：尝试清除默认设置并触发选择器（适用于小米等系统）
     */
    private fun tryClearDefaultAndChoose(context: Context): Boolean {
        return try {
            // 小米MIUI系统特殊处理
            if (isMiuiSystem()) {
                return tryMiuiExitLauncher(context)
            }
            
            // 其他系统尝试重置默认应用
            val intent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
                addCategory(Intent.CATEGORY_DEFAULT)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or 
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or 
                        Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
            }
            
            // 强制显示应用选择器
            val resolveInfo = context.packageManager.resolveActivity(intent, 0)
            if (resolveInfo != null && resolveInfo.activityInfo.packageName != context.packageName) {
                context.startActivity(intent)
                return true
            }
            
            false
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * 检查是否是小米MIUI系统
     */
    private fun isMiuiSystem(): Boolean {
        return try {
            val prop = Class.forName("android.os.SystemProperties")
            val method = prop.getMethod("get", String::class.java)
            val miui = method.invoke(null, "ro.miui.ui.version.name") as String?
            !miui.isNullOrEmpty()
        } catch (e: Exception) {
            // 备用检查方法
            android.os.Build.MANUFACTURER.equals("xiaomi", ignoreCase = true) ||
            android.os.Build.BRAND.equals("xiaomi", ignoreCase = true)
        }
    }
    
    /**
     * 小米MIUI系统特殊处理
     */
    private fun tryMiuiExitLauncher(context: Context): Boolean {
        return try {
            // 方法1：尝试打开小米的默认应用设置
            val miuiIntent = Intent().apply {
                action = "miui.intent.action.APP_PERM"
                putExtra("extra_pkgname", context.packageName)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            
            if (isIntentAvailable(context, miuiIntent)) {
                context.startActivity(miuiIntent)
                Toast.makeText(context, "请在\"默认启动\"中取消设置", Toast.LENGTH_LONG).show()
                return true
            }
            
            // 方法2：尝试启动小米桌面
            val miuiLauncherIntent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
                component = ComponentName("com.miui.home", "com.miui.home.launcher.Launcher")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            
            if (isIntentAvailable(context, miuiLauncherIntent)) {
                context.startActivity(miuiLauncherIntent)
                return true
            }
            
            // 方法3：尝试通用的小米桌面包名
            val xiaomiLaunchers = listOf(
                "com.miui.home/.launcher.Launcher",
                "com.mi.android.globallauncher/.ExtendedLauncher",
                "com.miui.miuilite/.Launcher"
            )
            
            for (launcher in xiaomiLaunchers) {
                val parts = launcher.split("/")
                if (parts.size == 2) {
                    val intent = Intent(Intent.ACTION_MAIN).apply {
                        addCategory(Intent.CATEGORY_HOME)
                        component = ComponentName(parts[0], parts[0] + parts[1])
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    }
                    
                    if (isIntentAvailable(context, intent)) {
                        context.startActivity(intent)
                        return true
                    }
                }
            }
            
            false
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * 检查Intent是否可用
     */
    private fun isIntentAvailable(context: Context, intent: Intent): Boolean {
        return try {
            val resolveInfo = context.packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
            resolveInfo != null
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * 打开桌面选择器，让用户选择默认桌面
     */
    private fun openLauncherChooser(context: Context) {
        tryLaunchSystemChooser(context)
    }

    /**
     * 获取系统默认桌面的包名（不包括当前应用）
     */
    fun getSystemDefaultLauncher(context: Context): String? {
        val launchers = getAvailableLaunchers(context)
        
        // 尝试找到系统自带的启动器
        val systemLaunchers = launchers.filter { resolveInfo ->
            val packageName = resolveInfo.activityInfo.packageName
            // 常见的系统桌面包名
            packageName.contains("launcher", ignoreCase = true) ||
            packageName.contains("home", ignoreCase = true) ||
            packageName.startsWith("com.android") ||
            packageName.startsWith("com.google.android") ||
            packageName.startsWith("com.samsung") ||
            packageName.startsWith("com.huawei") ||
            packageName.startsWith("com.xiaomi") ||
            packageName.startsWith("com.oppo") ||
            packageName.startsWith("com.vivo")
        }
        
        return systemLaunchers.firstOrNull()?.activityInfo?.packageName
            ?: launchers.firstOrNull()?.activityInfo?.packageName
    }

    /**
     * 启动指定的桌面应用
     */
    fun launchSpecificLauncher(context: Context, packageName: String, className: String) {
        try {
            val intent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
                component = ComponentName(packageName, className)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "无法启动指定的桌面应用", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * 重置为系统默认桌面
     */
    fun resetToSystemLauncher(context: Context) {
        val systemLauncher = getSystemDefaultLauncher(context)
        if (systemLauncher != null) {
            val launchers = getAvailableLaunchers(context)
            val targetLauncher = launchers.find { it.activityInfo.packageName == systemLauncher }
            
            if (targetLauncher != null) {
                launchSpecificLauncher(
                    context,
                    targetLauncher.activityInfo.packageName,
                    targetLauncher.activityInfo.name
                )
            } else {
                exitLauncherMode(context)
            }
        } else {
            exitLauncherMode(context)
        }
    }
}
