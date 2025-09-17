package tech.huangsh.onetap.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/**
 * 系统图标工具类
 */
object SystemIconUtils {
    
    /**
     * 获取系统相册应用的图标
     */
    fun getGalleryAppIcon(context: Context): Drawable? {
        return try {
            // 尝试获取系统相册应用
            val galleryIntents = listOf(
                "com.google.android.gallery3d", // Google相册
                "com.android.gallery3d", // 系统相册
                "com.miui.gallery", // MIUI相册
                "com.huawei.photos", // 华为相册
                "com.samsung.android.gallery3d", // 三星相册
                "com.oppo.gallery3d", // OPPO相册
                "com.vivo.gallery" // Vivo相册
            )
            
            // 尝试每个包名
            for (packageName in galleryIntents) {
                try {
                    val packageManager = context.packageManager
                    val appInfo = packageManager.getApplicationInfo(packageName, 0)
                    return packageManager.getApplicationIcon(appInfo)
                } catch (e: PackageManager.NameNotFoundException) {
                    continue
                }
            }
            
            // 如果都找不到，尝试通过Intent查找
            val intent = Intent(Intent.ACTION_PICK).apply {
                type = "image/*"
            }
            val resolveInfo = context.packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
            resolveInfo?.activityInfo?.let { activityInfo ->
                context.packageManager.getApplicationIcon(activityInfo.packageName)
            }
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * 获取系统相机应用的图标
     */
    fun getCameraAppIcon(context: Context): Drawable? {
        return try {
            // 尝试获取系统相机应用
            val cameraPackages = listOf(
                "com.google.android.GoogleCamera", // Google相机
                "com.android.camera2", // 系统相机2
                "com.android.camera", // 系统相机
                "com.miui.camera", // MIUI相机
                "com.huawei.camera", // 华为相机
                "com.samsung.android.camera", // 三星相机
                "com.oppo.camera", // OPPO相机
                "com.vivo.camera" // Vivo相机
            )
            
            // 尝试每个包名
            for (packageName in cameraPackages) {
                try {
                    val packageManager = context.packageManager
                    val appInfo = packageManager.getApplicationInfo(packageName, 0)
                    return packageManager.getApplicationIcon(appInfo)
                } catch (e: PackageManager.NameNotFoundException) {
                    continue
                }
            }
            
            // 如果都找不到，尝试通过Intent查找
            val intent = Intent("android.media.action.IMAGE_CAPTURE")
            val resolveInfo = context.packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
            resolveInfo?.activityInfo?.let { activityInfo ->
                context.packageManager.getApplicationIcon(activityInfo.packageName)
            }
        } catch (e: Exception) {
            null
        }
    }
}

/**
 * Compose中获取系统相册图标的便捷函数
 */
@Composable
fun rememberGalleryAppIcon(): Drawable? {
    val context = LocalContext.current
    return remember {
        SystemIconUtils.getGalleryAppIcon(context)
    }
}

/**
 * Compose中获取系统相机图标的便捷函数
 */
@Composable
fun rememberCameraAppIcon(): Drawable? {
    val context = LocalContext.current
    return remember {
        SystemIconUtils.getCameraAppIcon(context)
    }
}

