package tech.huangsh.onetap.utils

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

/**
 * 性能优化工具类
 */
object PerformanceUtils {
    
    private const val TAG = "PerformanceUtils"
    
    /**
     * 优化的图片加载器配置
     */
    fun getOptimizedImageLoader(context: Context): ImageLoader {
        return ImageLoader.Builder(context)
            .memoryCache {
                MemoryCache.Builder(context)
                    .maxSizePercent(0.25) // 使用25%的可用内存
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(context.cacheDir.resolve("image_cache"))
                    .maxSizeBytes(50 * 1024 * 1024) // 50MB
                    .build()
            }
            .respectCacheHeaders(false) // 忽略网络缓存头
            .networkCachePolicy(CachePolicy.ENABLED) // 启用网络缓存
            .memoryCachePolicy(CachePolicy.ENABLED) // 启用内存缓存
            .diskCachePolicy(CachePolicy.ENABLED) // 启用磁盘缓存
            .build()
    }
    
    /**
     * 压缩图片质量
     */
    fun compressBitmap(bitmap: Bitmap, quality: Int = 80): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)
        return stream.toByteArray()
    }
    
    /**
     * 计算合适的采样大小
     */
    fun calculateInSampleSize(
        options: android.graphics.BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        val (height: Int, width: Int) = options.run { outHeight to outWidth }
        var inSampleSize = 1
        
        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2
            
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        
        return inSampleSize
    }
    
    /**
     * 优化的Bitmap解码
     */
    suspend fun decodeOptimizedBitmap(
        data: ByteArray,
        reqWidth: Int,
        reqHeight: Int
    ): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val options = android.graphics.BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            
            android.graphics.BitmapFactory.decodeByteArray(data, 0, data.size, options)
            
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
            options.inJustDecodeBounds = false
            options.inPreferredConfig = Bitmap.Config.RGB_565 // 使用更节省内存的配置
            
            android.graphics.BitmapFactory.decodeByteArray(data, 0, data.size, options)
        } catch (e: Exception) {
            Log.e(TAG, "Error decoding bitmap", e)
            null
        }
    }
    
    /**
     * 清理内存
     */
    fun clearMemory(context: Context) {
        // 清理图片内存缓存
        System.gc()
        
        // 清理不必要的资源
        if (context is Activity) {
            context.finishAffinity()
        }
    }
    
    /**
     * 记录性能日志
     */
    inline fun <T> measureTime(tag: String, block: () -> T): T {
        val startTime = System.currentTimeMillis()
        val result = block()
        val endTime = System.currentTimeMillis()
        Log.d(tag, "Execution time: ${endTime - startTime}ms")
        return result
    }
}

/**
 * 记住的图片位图
 */
@Composable
fun rememberImageBitmap(
    data: ByteArray?,
    width: Int = 100,
    height: Int = 100
): ImageBitmap? {
    val context = LocalContext.current
    
    return remember(data) {
        data?.let {
            runBlocking {
                PerformanceUtils.decodeOptimizedBitmap(it, width, height)?.asImageBitmap()
            }
        }
    }
}