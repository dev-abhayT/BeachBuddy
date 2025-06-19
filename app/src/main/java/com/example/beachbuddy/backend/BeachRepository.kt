package com.example.beachbuddy.backend

import android.annotation.SuppressLint
import android.util.Log
import com.example.beachbuddy.BuildConfig
import com.example.beachbuddy.data.Beach
import com.example.beachbuddy.data.BeachLocation
import com.example.beachbuddy.data.CurrentVariables
import com.example.beachbuddy.data.FourSquarePhotos
import com.example.beachbuddy.data.MarineWeatherResponse
import com.example.beachbuddy.data.SafetyStatus
import com.example.beachbuddy.interfaces.FourSquareServices
import com.example.beachbuddy.interfaces.MarineWeatherAPIService
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
                retrofitPhoto = Retrofit.Builder()
                    .baseUrl("https://api.foursquare.com/v3/places/${beachLocation.fsq_id}/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                val photosApi = retrofitPhoto.create(FourSquareServices::class.java)


                val photoUrl = photosApi.getPhotos(1).firstOrNull() ?: FourSquarePhotos("","")

//                val gm = GenerativeModel(
//                    modelName = "gemini-2.5-flash-preview-05-20",
//                    apiKey = BuildConfig.GEMINI_API_KEY
//                )
//
//                val prompt = "Give me only a short description in 30-35 words about $beachLocation.name} located at latitude ${beachLocation.geocodes.main.latitude} and longitude ${beachLocation.geocodes.main.longitude}."
//                val response = gm.generateContent(prompt)




                val distance = calculateDistance(
                    userLat, userLng,
                    beachLocation.latitude,
                    beachLocation.longitude
                )

                createBeachFromData(beachLocation, weatherData, distance, photoUrl, "")

            }

        } catch (e: Exception){
            Log.e(TAG,"Exception Occurred trying to get beaches: ${e.message}")
           return emptyList()
        }
    }

    @SuppressLint("DefaultLocale")
    private fun createBeachFromData(location: BeachLocation, weatherData: CurrentVariables?, distance: Double, photoUrl: FourSquarePhotos, modelResponse : String): Beach{
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

        val url2 = photoUrl.suffix
        val url = photoUrl.prefix + "original" + url2



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