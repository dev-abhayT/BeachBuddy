package com.example.beachbuddy.data

data class Places(
    val result : List<PlacesResult>
)

data class PlacesResult(
    val name : String?="",
    val address : String?="",
    val latLng : LatLng,
    val photos : List<Photo>
)

data class LatLng(
    val location : Loc
)

data class Loc(
    val latitude : Double,
    val longitude : Double
)

data class Photo(
    val photoRef : String?= ""
)