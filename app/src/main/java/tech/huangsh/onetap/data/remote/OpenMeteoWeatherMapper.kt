package tech.huangsh.onetap.data.remote

import tech.huangsh.onetap.data.model.WeatherInfo

/**
 * Utility class to convert Open-Meteo API response to app's WeatherInfo model
 */
object OpenMeteoWeatherMapper {
    
    /**
     * Convert Open-Meteo weather response to app's WeatherInfo model
     */
    fun mapToWeatherInfo(response: OpenMeteoWeatherResponse, city: String = "北京"): WeatherInfo {
        return WeatherInfo(
            temperature = response.current.temperature_2m.toInt(),
            weather = getWeatherDescription(response.current.weather_code),
            weatherIcon = getWeatherIcon(response.current.weather_code),
            humidity = response.current.relative_humidity_2m,
            windSpeed = response.current.wind_speed_10m.toFloat(),
            city = city,
            updateTime = System.currentTimeMillis()
        )
    }
    
    /**
     * Map WMO weather code to weather description
     * https://open-meteo.com/en/docs
     */
    private fun getWeatherDescription(weatherCode: Int): String {
        return when (weatherCode) {
            0 -> "晴"
            1 -> "晴"
            2 -> "少云"
            3 -> "多云"
            45, 48 -> "雾"
            51, 53, 55 -> "毛毛雨"
            56, 57 -> "冻雨"
            61, 63, 65 -> "小雨"
            66, 67 -> "冻雨"
            71, 73, 75 -> "小雪"
            77 -> "雪粒"
            80, 81, 82 -> "中雨"
            85, 86 -> "中雪"
            95 -> "雷阵雨"
            96, 99 -> "雷阵雨"
            else -> "晴"
        }
    }
    
    /**
     * Map WMO weather code to weather icon
     */
    private fun getWeatherIcon(weatherCode: Int): String {
        return when (weatherCode) {
            0, 1 -> "☀️"
            2, 3 -> "⛅"
            45, 48 -> "🌫️"
            51, 53, 55, 56, 57 -> "🌦️"
            61, 63, 65, 66, 67 -> "🌧️"
            71, 73, 75, 77 -> "❄️"
            80, 81, 82 -> "🌧️"
            85, 86 -> "❄️"
            95, 96, 99 -> "⛈️"
            else -> "🌤️"
        }
    }
}