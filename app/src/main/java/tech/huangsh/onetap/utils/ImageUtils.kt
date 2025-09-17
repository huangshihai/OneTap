package tech.huangsh.onetap.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.Log
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.UUID

/**
 * 图片工具类
 */
object ImageUtils {
    
    /**
     * 从Uri获取Bitmap
     */
    fun getBitmapFromUri(context: Context, uri: Uri): Bitmap? {
        return try {
            when (uri.scheme) {
                "content" -> {
                    context.contentResolver.openInputStream(uri)?.use { inputStream ->
                        BitmapFactory.decodeStream(inputStream)
                    }
                }
                "file" -> {
                    BitmapFactory.decodeFile(uri.path)
                }
                else -> null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Bitmap转ByteArray
     */
    fun bitmapToByteArray(bitmap: Bitmap, quality: Int = 100): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, quality, stream)
        return stream.toByteArray()
    }

    /**
     * ByteArray转Bitmap
     */
    fun byteArrayToBitmap(byteArray: ByteArray): Bitmap? {
        return try {
            BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Drawable转Bitmap
     */
    fun drawableToBitmap(drawable: Drawable): Bitmap {
        if (drawable is BitmapDrawable) {
            return drawable.bitmap
        }
        
        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = android.graphics.Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    /**
     * 保存Bitmap到文件
     */
    fun saveBitmapToFile(bitmap: Bitmap, file: File): Boolean {
        return try {
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.flush()
            outputStream.close()
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }

    /**
     * 调整图片大小
     */
    fun resizeBitmap(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        
        if (width <= maxWidth && height <= maxHeight) {
            return bitmap
        }
        
        val ratio = minOf(maxWidth.toFloat() / width, maxHeight.toFloat() / height)
        val newWidth = (width * ratio).toInt()
        val newHeight = (height * ratio).toInt()
        
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

    /**
     * 创建圆形Bitmap
     */
    fun createCircularBitmap(bitmap: Bitmap): Bitmap {
        val size = minOf(bitmap.width, bitmap.height)
        val output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(output)
        
        val paint = android.graphics.Paint().apply {
            isAntiAlias = true
            color = android.graphics.Color.BLACK
        }
        
        val rect = android.graphics.Rect(0, 0, size, size)
        canvas.drawOval(android.graphics.RectF(rect), paint)
        
        paint.xfermode = android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(bitmap, rect, rect, paint)
        
        return output
    }
    
    /**
     * 将URI中的图片复制到应用私有目录
     * @param context 应用上下文
     * @param uri 图片URI
     * @return 复制后的文件路径，如果复制失败返回null
     */
    fun copyImageToAppDirectory(context: Context, uri: Uri): String? {
        try {
            // 创建头像存储目录
            val avatarsDir = File(context.filesDir, "avatars")
            if (!avatarsDir.exists()) {
                avatarsDir.mkdirs()
            }
            
            // 生成唯一文件名
            val fileName = "avatar_${UUID.randomUUID()}.jpg"
            val outputFile = File(avatarsDir, fileName)
            
            // 复制文件
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(outputFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            
            // 压缩图片以节省空间
            compressImage(outputFile, 800, 800)
            
            Log.d("ImageUtils", "Image copied to: ${outputFile.absolutePath}")
            return outputFile.absolutePath
            
        } catch (e: Exception) {
            Log.e("ImageUtils", "Failed to copy image", e)
            return null
        }
    }
    
    /**
     * 压缩图片
     * @param file 图片文件
     * @param maxWidth 最大宽度
     * @param maxHeight 最大高度
     */
    private fun compressImage(file: File, maxWidth: Int, maxHeight: Int) {
        try {
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            
            BitmapFactory.decodeFile(file.absolutePath, options)
            
            // 计算采样率
            var inSampleSize = 1
            if (options.outHeight > maxHeight || options.outWidth > maxWidth) {
                val halfHeight = options.outHeight / 2
                val halfWidth = options.outWidth / 2
                
                while (halfHeight / inSampleSize >= maxHeight && halfWidth / inSampleSize >= maxWidth) {
                    inSampleSize *= 2
                }
            }
            
            options.inJustDecodeBounds = false
            options.inSampleSize = inSampleSize
            
            val bitmap = BitmapFactory.decodeFile(file.absolutePath, options)
            
            if (bitmap != null) {
                FileOutputStream(file).use { outputStream ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
                }
                bitmap.recycle()
            }
            
        } catch (e: Exception) {
            Log.e("ImageUtils", "Failed to compress image", e)
        }
    }
    
    /**
     * 删除指定路径的图片文件
     * @param imagePath 图片文件路径
     */
    fun deleteImageFile(imagePath: String?) {
        if (imagePath != null) {
            try {
                val file = File(imagePath)
                if (file.exists()) {
                    file.delete()
                    Log.d("ImageUtils", "Image deleted: $imagePath")
                }
            } catch (e: Exception) {
                Log.e("ImageUtils", "Failed to delete image", e)
            }
        }
    }
    
    /**
     * 检查图片文件是否存在
     * @param imagePath 图片文件路径
     * @return 文件是否存在
     */
    fun isImageFileExists(imagePath: String?): Boolean {
        return imagePath?.let { File(it).exists() } ?: false
    }
}