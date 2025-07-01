package com.example.beachbuddy.backend

import android.annotation.SuppressLint
import android.util.Log
import androidx.room.Room
import com.example.beachbuddy.BuildConfig
import com.example.beachbuddy.data.Beach
import com.example.beachbuddy.data.BeachLocation
import com.example.beachbuddy.data.CurrentVariables
import com.example.beachbuddy.data.Icon
import com.example.beachbuddy.data.MarineWeatherResponse
import com.example.beachbuddy.data.SafetyStatus
import com.example.beachbuddy.data.UnsplashResponse
import com.example.beachbuddy.data.room.BeachEntity
import com.example.beachbuddy.interfaces.FourSquareServices
import com.example.beachbuddy.interfaces.MarineWeatherAPIService
import com.example.beachbuddy.interfaces.UnsplashServices
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class BeachRepository(private val goMapsService : FourSquareServices,
                      private val goMapsKey : String,
                      private val marineWeatherService : MarineWeatherAPIService) {
    private lateinit var retrofitPhoto : Retrofit


    companion object{
        const val TAG = "BeachRepository"
    }


    @SuppressLint("DefaultLocale")
    suspend fun getBeaches(userLat:Double, userLng:Double) : List<Beach> {
        try {
            val lat = String.format("%.2f",userLat).toDouble()
            val lng = String.format("%.2f",userLng).toDouble()
            retrofitPhoto = Retrofit.Builder()
                .baseUrl("https://api.unsplash.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            val photosApi = retrofitPhoto.create(UnsplashServices::class.java)
            var count = 0




            val url = photosApi.getPhotos()

            val location = "${lat},${lng}"
            Log.e(TAG,location)

            val beachLocations = goMapsService.getNearbyBeaches("beaches",location)
            return beachLocations.results.map { beachLocation ->
                val weatherData = try{
                    marineWeatherService.getMarineData(beachLocation.latitude, beachLocation.longitude).currentVariables}
                catch (e:Exception){
                    Log.e(TAG,"Exception Occurred trying to get marine data: ${e.message}")
                    MarineWeatherResponse(CurrentVariables(0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0)).currentVariables

                }

                val distance = calculateDistance(
                    userLat, userLng,
                    beachLocation.latitude,
                    beachLocation.longitude
                )
                val photoUrl = url.result[count].urls.regular
                count = count + 1

                createBeachFromData(beachLocation, weatherData, distance, photoUrl, "")

            }

        } catch (e: Exception){
            Log.e(TAG,"Exception Occurred trying to get beaches: ${e.message}")
           return emptyList()
        }
    }

    @SuppressLint("DefaultLocale")
    private fun createBeachFromData(location: BeachLocation, weatherData: CurrentVariables?, distance: Double, photoUrl: String, modelResponse : String): Beach{
        val waveHeight = weatherData?.waveHeight ?: 0.5
        //val waterTemp = weatherData?.waterTemperature ?: 24.0
        //val currentVel = weatherData?.currentVelocity ?: 0.2
        val wavePeriod = weatherData?.wavePeriod ?: 8.0
        val windWaveHeight = weatherData?.windWaveHeight ?: 0.0
        val windWavePeriod = weatherData?.windWavePeriod ?: 0.0
        val swellWaveHeight = weatherData?.swellWaveHeight ?: 0.0
        val swellWavePeriod = weatherData?.swellWavePeriod ?: 0.0
        val seaSurfaceTemperature = weatherData?.seaSurfaceTemperature ?: 24.0
        val oceanCurrentVelocity = weatherData?.oceanCurrentVelocity ?: 0.0

        val url = photoUrl

        val safetyResult = BeachClassifier(waveHeight, wavePeriod, windWaveHeight, windWavePeriod, swellWaveHeight, swellWavePeriod, seaSurfaceTemperature, oceanCurrentVelocity).classifyBeach()

        return Beach(
            id = location.fsq_id,
            name = location.name,
            address = location.location.formattedAddress ?: "Unknown location",
            distance = String.format("%.1fkm", distance),
            latitude = location.latitude,
            longitude = location.longitude,
            safetyStatus = safetyResult.safetyStatus,
            safetyReason = safetyResult.summary,
            waveHeight = waveHeight,
            wavePeriod = wavePeriod,
            photoUrl = url,
            shortDescription = modelResponse,
            windWaveHeight = windWaveHeight,
            windWavePeriod = windWavePeriod,
            swellWaveHeight = swellWaveHeight,
            swellWavePeriod = swellWavePeriod,
            seaSurfaceTemperature = seaSurfaceTemperature,
            oceanCurrentVelocity = oceanCurrentVelocity,
            tip = safetyResult.tip,
            beachActivity = safetyResult.beachActivity,
            variableStatus = safetyResult.variableStatus
        )


    }


    private fun calculateDistance(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        val earthRadius = 6371 // km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLng = Math.toRadians(lng2 - lng1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLng / 2) * sin(dLng / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return earthRadius * c
    }
}