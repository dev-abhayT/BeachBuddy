package com.example.beachbuddy.data

data class FilterLocations(val name: String, var lat: Double, var lng: Double)

var selectedIndex = 0

var fallbackLocations = listOf(
    FilterLocations("Your Current Location", 0.0, 0.0),  // Default
    FilterLocations("Mumbai, Maharashtra", 19.0760, 72.8777),
    FilterLocations("Goa (Palolem/Baga)", 15.3970, 73.7954),
    FilterLocations("Digha, West Bengal", 21.6200, 87.5300),
    FilterLocations("Puri, Odisha", 19.8135, 85.8312),
    FilterLocations("Gopalpur, Odisha", 19.2829, 84.9109),
    FilterLocations("Visakhapatnam, Andhra Pradesh", 17.7041, 83.2977),
    FilterLocations("Porbandar, Gujarat", 21.6417, 69.6074),
    FilterLocations("Chennai (Marina), Tamil Nadu", 13.0639, 80.2824),
    FilterLocations("Pondicherry", 11.9416, 79.8083),
    FilterLocations("Malpe, Karnataka", 13.3500, 74.7000),
    FilterLocations("Karwar, Karnataka", 14.8020, 74.1335),
    FilterLocations("Kochi (Cherai), Kerala", 9.9312, 76.2673),
    FilterLocations("Alappuzha, Kerala", 9.4981, 76.3388),
    FilterLocations("Thoothukudi, Tamil Nadu", 8.7642, 78.1348),
    FilterLocations("Rameswaram, Tamil Nadu", 9.2876, 79.3129)
)
