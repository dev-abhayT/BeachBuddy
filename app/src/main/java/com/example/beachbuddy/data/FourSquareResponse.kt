package com.example.beachbuddy.data

import com.google.gson.annotations.SerializedName

data class FourSquareResponse(
    @SerializedName("results")
    val results: List<BeachLocation>
)

data class FourSquarePhotos(
    @SerializedName("prefix")
    val prefix: String,
    @SerializedName("suffix")
    val suffix: String
)


data class BeachLocation(
    @SerializedName("fsq_place_id")
    val fsq_id: String,
    @SerializedName("latitude")
    val latitude: Double,
    @SerializedName("longitude")
    val longitude: Double,
    @SerializedName("location")
    val location: Location,
    @SerializedName("name")
    val name: String
)



data class Location(
    @SerializedName("formatted_address")
    val formattedAddress : String
)



