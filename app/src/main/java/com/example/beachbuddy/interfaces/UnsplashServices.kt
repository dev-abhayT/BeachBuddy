package com.example.beachbuddy.interfaces

import com.example.beachbuddy.BuildConfig
import com.example.beachbuddy.data.UnsplashResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface UnsplashServices {
    @GET("search/photos")
    suspend fun getPhotos(
        @Query("query") query: String = "Beaches",
        @Query("page") page: Int = 1,
        @Query("per_page") per_page: Int = 15,
        @Query("client_id") client_id: String = BuildConfig.UNSPLASH_API_KEY
    ) : UnsplashResponse

}