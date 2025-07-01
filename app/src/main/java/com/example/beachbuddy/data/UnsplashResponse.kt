package com.example.beachbuddy.data

import com.google.gson.annotations.SerializedName

data class UnsplashResponse(
    @SerializedName("results")
    var result : List<Results>
)

data class Results(
    @SerializedName("urls")
    val urls : Urls
)

data class Urls(
    @SerializedName("raw")
    val raw : String,
    @SerializedName("regular")
    val regular : String

)