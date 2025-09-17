package tech.huangsh.onetap.data.model

/**
 * 天气信息数据模型
 */
data class WeatherInfo(
    val temperature: Int = 25,
    val weather: String = "晴",
    val weatherIcon: String = "☀️",
    val humidity: Int = 50,
    val windSpeed: Float = 3.0f,
    val city: String = "北京",
    val updateTime: Long = System.currentTimeMillis()
)