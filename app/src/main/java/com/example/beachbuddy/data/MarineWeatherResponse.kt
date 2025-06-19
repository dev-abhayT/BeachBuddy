package com.example.beachbuddy.data

import com.google.gson.annotations.SerializedName

data class MarineWeatherResponse(
    @SerializedName("current")
    val currentVariables : CurrentVariables
)

data class CurrentVariables(
    @SerializedName("wave_height")
    val waveHeight : Double,
    @SerializedName("wave_period")
    val wavePeriod : Double,
    @SerializedName("wind_wave_height")
    val windWaveHeight : Double,
    @SerializedName("wind_wave_period")
    val windWavePeriod : Double,
    @SerializedName("swell_wave_height")
    val swellWaveHeight : Double,
    @SerializedName("swell_wave_period")
    val swellWavePeriod : Double,
    @SerializedName("sea_surface_temperature")
    val seaSurfaceTemperature : Double,
    @SerializedName("ocean_current_velocity")
    val oceanCurrentVelocity : Double
)

data class WeatherForecastResponse(
    @SerializedName("current")
    var currentForecast : CurrentForecast,
    @SerializedName("daily")
    var dailyForecast: DailyForecast
)

data class CurrentForecast(
    @SerializedName("temperature_2m")
    val temperature : Double,
    @SerializedName("relative_humidity_2m")
    val humidity : Double,
    @SerializedName("wind_speed_10m")
    val windSpeed : Double,
    @SerializedName("wind_gusts_10m")
    val windGusts : Double,
    @SerializedName("precipitation")
    val precipitation : Double
)

data class DailyForecast(
    @SerializedName("sunrise")
    val sunrise : List<String>,
    @SerializedName("sunset")
    val sunset : List<String>,
    @SerializedName("uv_index_max")
    val uvIndexMax : List<Double>)