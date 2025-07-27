package com.example.beachbuddy.WorkManager

import android.content.Context
import androidx.room.Room
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.beachbuddy.Notifications.NotificationCreator
import com.example.beachbuddy.backend.BeachClassifier
import com.example.beachbuddy.backend.BeachRepository
import com.example.beachbuddy.data.room.BeachEntity
import com.example.beachbuddy.data.room.BeachLocalDB
import com.example.beachbuddy.interfaces.MarineWeatherAPIService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class BeachStatusWorker(val context : Context, val params : WorkerParameters) : Worker(context,params) {
    private lateinit var db : BeachLocalDB


    override fun doWork(): Result {
        db = Room.databaseBuilder(context,BeachLocalDB::class.java,"beach_db").build()
        val retrofitMarine = Retrofit.Builder()
            .baseUrl("https://marine-api.open-meteo.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val marineWeatherApi = retrofitMarine.create(MarineWeatherAPIService::class.java)
        return try{
            CoroutineScope(Dispatchers.IO).launch {
                val beaches = db.beachDAO().getAllBeaches()
                beaches.forEach{beach ->
                    val latitude = beach.latitude
                    val longitude = beach.longitude
                    val weatherData = marineWeatherApi.getMarineData(latitude, longitude).currentVariables
                    val waveHeight = weatherData.waveHeight ?: 0.5
                    val wavePeriod = weatherData.wavePeriod ?: 8.0
                    val windWaveHeight = weatherData.windWaveHeight ?: 0.0
                    val windWavePeriod = weatherData.windWavePeriod ?: 0.0
                    val swellWaveHeight = weatherData.swellWaveHeight ?: 0.0
                    val swellWavePeriod = weatherData.swellWavePeriod ?: 0.0
                    val seaSurfaceTemperature = weatherData.seaSurfaceTemperature ?: 24.0
                    val oceanCurrentVelocity = weatherData.oceanCurrentVelocity ?: 0.0

                    val safetyResult = BeachClassifier(waveHeight, wavePeriod, windWaveHeight, windWavePeriod, swellWaveHeight, swellWavePeriod, seaSurfaceTemperature, oceanCurrentVelocity).classifyBeach()
                    if(beach.currentStatus != safetyResult.safetyStatus){
                        val beachEntity = BeachEntity(beach.id,beach.name,beach.latitude,beach.longitude,safetyResult.safetyStatus)
                        db.beachDAO().insertBeach(beachEntity)
                        val notificationHelper = NotificationCreator(context)
                        notificationHelper.showNotifications("Beach Alert!","${beach.name}'s safety status is now ${safetyResult.safetyStatus}")
                    }



                }
            }
            Result.success()
        }catch (e:Exception){
            Result.failure()

        }





    }
}