package tech.huangsh.onetap.data.remote

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import tech.huangsh.onetap.data.model.WeatherInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 天气服务 - 使用 Open-Meteo API
 */
@Singleton
class WeatherService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private val openMeteoApiService: OpenMeteoApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.open-meteo.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        
        retrofit.create(OpenMeteoApiService::class.java)
    }
    
    /**
     * 获取天气信息
     */
    suspend fun getWeatherInfo(): WeatherInfo {
        return try {
            // 获取位置信息
            val location = getLocation()
            val city = getLocationCity() ?: "北京"
            
            if (location != null) {
                // 使用 Open-Meteo API 获取真实天气数据
                val response = openMeteoApiService.getCurrentWeather(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    timezone = "Asia/Shanghai"
                )
                
                OpenMeteoWeatherMapper.mapToWeatherInfo(response, city)
            } else {
                // 无法获取位置时，使用默认位置（北京）
                val defaultResponse = openMeteoApiService.getCurrentWeather(
                    latitude = 39.9042,
                    longitude = 116.4074,
                    timezone = "Asia/Shanghai"
                )
                
                OpenMeteoWeatherMapper.mapToWeatherInfo(defaultResponse, "北京")
            }
        } catch (e: Exception) {
            Log.e("WeatherService", "获取天气数据失败", e)
            // API 调用失败时返回默认值
            WeatherInfo(
                temperature = 25,
                weather = "晴",
                weatherIcon = "☀️",
                humidity = 50,
                windSpeed = 3.0f,
                city = "北京",
                updateTime = System.currentTimeMillis()
            )
        }
    }
    
    /**
     * 获取位置信息
     */
    @RequiresPermission(anyOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private fun getLocation(): Location? {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return null
        }
        
        try {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            return locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
        } catch (e: Exception) {
            Log.e("WeatherService", "获取位置失败", e)
            return null
        }
    }
    
    /**
     * 获取位置城市名
     */
    @RequiresPermission(anyOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private fun getLocationCity(): String? {
        // 简化处理，实际项目中应该使用地理编码服务
        return try {
            val location = getLocation()
            location?.let { "北京" }
        } catch (e: Exception) {
            Log.e("WeatherService", "获取城市名失败", e)
            null
        }
    }
    
    /**
     * 监听天气变化（每小时更新一次）
     */
    fun observeWeatherChanges(): Flow<WeatherInfo> = flow {
        while (true) {
            emit(getWeatherInfo())
            delay(60 * 60 * 1000) // 1小时更新一次
        }
    }
}