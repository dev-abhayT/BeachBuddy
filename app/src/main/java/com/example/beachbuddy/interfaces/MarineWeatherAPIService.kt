package com.example.beachbuddy.interfaces

import com.example.beachbuddy.data.MarineWeatherResponse
import com.example.beachbuddy.data.WeatherForecastResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface MarineWeatherAPIService {
@GET("v1/marine")
suspend fun getMarineData(
    @Query("latitude") latitude: Double,
    @Query("longitude") longitude: Double,
    @Query("current") variables: String = "wave_height,wave_period,wind_wave_height,wind_wave_period,swell_wave_height,swell_wave_period,sea_surface_temperature,ocean_current_velocity",
    @Query("timezone") timezone: String? = "Asia/Kolkata"
) : MarineWeatherResponse

@GET("v1/forecast")
suspend fun getWeatherForecast(
    @Query("latitude") latitude: Double,
    @Query("longitude") longitude: Double,
    @Query("current") currentVar: String = "temperature_2m,relative_humidity_2m,wind_speed_10m,wind_gusts_10m,precipitation",
    @Query("daily") dailyVar : String = "sunrise,sunset,uv_index_max",
    @Query("forecast_days") days:Int = 1,
    @Query("timezone") timezone: String = "Asia/Kolkata"
) : WeatherForecastResponse


}