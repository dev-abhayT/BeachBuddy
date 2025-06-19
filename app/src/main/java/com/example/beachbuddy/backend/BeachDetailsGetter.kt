package com.example.beachbuddy.backend

import android.os.Build
import android.util.Log
import com.example.beachbuddy.BuildConfig
import com.example.beachbuddy.data.Beach
import com.example.beachbuddy.data.BeachQuickFacts
import com.example.beachbuddy.data.WeatherForecastResponse
import com.example.beachbuddy.interfaces.MarineWeatherAPIService
import com.google.ai.client.generativeai.GenerativeModel
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class BeachDetailsGetter(val beach : Beach) {
    companion object{
        const val TAG = "BeachDetailsGetter"
    }
    private val gm = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = BuildConfig.GEMINI_API_KEY
    )
    private val gm2 = GenerativeModel(
        modelName = "gemini-2.0-flash",
        apiKey = BuildConfig.GEMINI_API_KEY
    )
    suspend fun getDescription() : String {
                val prompt = "Give me only a short technical description in 30-35 words about ${beach.name} located at latitude ${beach.latitude} and longitude ${beach.longitude}. Include only interesting/safe facts about the beach."
                val response = gm.generateContent(prompt)

        return response.text.toString()
    }

    suspend fun getSafetyTips() : String {
        val prompt = "Give me a short bulleted list(3-4 points) of one liner safety tips for a visitor who wants to visit ${beach.name} located at latitude ${beach.latitude} and longitude ${beach.longitude}, and no other text. Use newline char and spaces for better display"
        val response = gm.generateContent(prompt)

        return response.text.toString()

    }

    suspend fun getWeatherForecast() : WeatherForecastResponse {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.open-meteo.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val weatherApi = retrofit.create(MarineWeatherAPIService::class.java)

        return weatherApi.getWeatherForecast(beach.latitude,beach.longitude)


    }

    fun convertTime(isoString : String) : String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.getDefault())
        val outputFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

        return try {
            val date = inputFormat.parse(isoString)
            outputFormat.format(date!!)
        } catch (e: Exception) {
            "Invalid time"
        }
    }

    fun classifyUV(uvIndex : Double) : ClassifyUVIndex{
        return when {
            uvIndex <= 2.0 -> ClassifyUVIndex.LOW
            uvIndex <= 5.0 -> ClassifyUVIndex.MODERATE
            uvIndex <= 7.0 -> ClassifyUVIndex.HIGH
            uvIndex <= 10.0 -> ClassifyUVIndex.VERY_HIGH
            else -> ClassifyUVIndex.EXTREME
        }
    }

    suspend fun getQuickFacts() : String {
        try{
            val prompt = "Give me the following information about ${beach.name} at ${beach.latitude}N,${beach.longitude}E in JSON format.\n" +
                    "If any field is not known, use \"Data Not Available\" for strings. Answers should be 1-2 worded, use short forms for months\n" +
                    "Only return valid JSON with the following structure:\n" +
                    "\n" +
                    "{\n" +
                    "  \"best_time_to_visit\": \"\",\n" +
                    "  \"beach_type\": \"\",\n" +
                    "  \"facilities\": \"\" ,\n" +
                    "  \"popular_for\": \"\" \n" +
                    "}"
            val response = gm2.generateContent(prompt)
            return response.text.toString()

        }catch (e:java.lang.Exception){
            return ""
        }
    }

//    suspend fun getBestTime() : String {
//        try {
//            val prompt = "Give Me the best time to visit(months) ${beach.name} at ${beach.latitude}N,${beach.longitude}E. Dont include any other text."
//            val response = gm2.generateContent(prompt)
//            return response.text.toString()
//        }catch (e:java.lang.Exception){
//            Log.e(TAG, "DID NOT GET BEST TIME ${e.message}")
//            return "Data Not Available"
//        }
    }




enum class ClassifyUVIndex(val displayName : String, val color : String, val tip : String){
    LOW("Low", "#10B981", "Low risk. No protection needed."),
    MODERATE("Moderate", "#FACC15", "Use sunglasses and SPF 30+ sunscreen."),
    HIGH("High", "#F97316", "Seek shade during midday hours."),
    VERY_HIGH("Very High", "#EF4444", "Wear a hat, sunglasses, and SPF 50+."),
    EXTREME("Extreme", "#8B5CF6", "Avoid sun exposure. Maximum protection needed.")
}