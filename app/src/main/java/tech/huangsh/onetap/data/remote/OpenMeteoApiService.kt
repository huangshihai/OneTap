package tech.huangsh.onetap.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Open-Meteo API service interface
 * https://open-meteo.com/
 */
interface OpenMeteoApiService {
    
    /**
     * Get current weather data
     * @param latitude Latitude
     * @param longitude Longitude
     * @param temperatureUnit Temperature unit (celsius or fahrenheit)
     * @param windSpeedUnit Wind speed unit (kmh, ms, mph, or kn)
     * @param precipitationUnit Precipitation unit (mm or inch)
     * @param timezone Timezone (e.g., "auto", "UTC", "Asia/Shanghai")
     */
    @GET("v1/forecast")
    suspend fun getCurrentWeather(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("temperature_unit") temperatureUnit: String = "celsius",
        @Query("wind_speed_unit") windSpeedUnit: String = "ms",
        @Query("precipitation_unit") precipitationUnit: String = "mm",
        @Query("timezone") timezone: String = "auto",
        @Query("current") current: String = "temperature_2m,relative_humidity_2m,wind_speed_10m,weather_code"
    ): OpenMeteoWeatherResponse
}

/**
 * Open-Meteo API response data class
 */
data class OpenMeteoWeatherResponse(
    val latitude: Double,
    val longitude: Double,
    val timezone: String,
    val current: CurrentWeather,
    val current_units: CurrentUnits
)

data class CurrentWeather(
    val time: String,
    val interval: Int,
    val temperature_2m: Double,
    val relative_humidity_2m: Int,
    val wind_speed_10m: Double,
    val weather_code: Int
)

data class CurrentUnits(
    val time: String,
    val interval: String,
    val temperature_2m: String,
    val relative_humidity_2m: String,
    val wind_speed_10m: String,
    val weather_code: String
)