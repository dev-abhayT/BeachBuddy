package com.example.beachbuddy.interfaces

import com.example.beachbuddy.BuildConfig
import com.example.beachbuddy.data.FourSquarePhotos
import com.example.beachbuddy.data.FourSquareResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Query

interface FourSquareServices {
    @Headers(
        "authorization: Bearer ${BuildConfig.FSQ_API_KEY}",
        "accept: application/json",
        "X-Places-Api-Version: 2025-06-17"
    )

    @GET("places/search")
    suspend fun getNearbyBeaches(
        @Query("query") name: String = "beaches",
        @Query("ll") location: String,
        @Query("radius") radius: Int = 50000,
        @Query("fsq_category_ids") categories: String = "4bf58dd8d48988d1e2941735",
        @Query("limit") limit: Int = 10
    ): FourSquareResponse

    @GET("photos")
    suspend fun getPhotos(
        @Query("limit") limit: Int = 1,
        @Query("classifications") category: String = "outdoor",
        @Header("Authorization") apiKey: String = BuildConfig.FSQ_OLD_API_KEY

    ): List<FourSquarePhotos>
}

