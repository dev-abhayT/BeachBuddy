package com.example.beachbuddy.data

import android.os.Parcelable
import com.example.beachbuddy.backend.BeachActivity
import com.example.beachbuddy.backend.BeachReport
import com.example.beachbuddy.backend.VariableSafetyStatus
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Beach(
    val id: String,
    val name: String,
    val address: String,
    val distance: String,
    val latitude: Double,
    val longitude: Double,
    val safetyStatus: SafetyStatus,
    val safetyReason: String,
    val waveHeight: Double,
    val waterTemperature: Double?=0.0,
    val currentVelocity: Double?=0.0,
    val wavePeriod: Double,
    val isFavorite: Boolean = false,
    val photoUrl: String,
    val shortDescription: String? = "",
    val windWaveHeight: Double,
    val windWavePeriod: Double,
    val windSpeed: Double?=0.0,
    val windGust: Double?=0.0,
    val swellWaveHeight: Double,
    val swellWavePeriod: Double,
    val seaSurfaceTemperature: Double,
    val oceanCurrentVelocity: Double,
    val tip: String,
    val beachActivity: BeachActivity,
    val  variableStatus : VariableSafetyStatus
) : Parcelable

@Parcelize
enum class SafetyStatus(val displayName: String, val color: String, val bgColor: String) : Parcelable {
    SAFE("Safe", "#10B981", "#90EE90"),
    CAUTION("Caution", "#F59E0B", "#FFD580"),
    DANGEROUS("Unsafe", "#FF474D", "#FFC1C3")
}

@Parcelize
data class BeachQuickFacts(
    @SerializedName("best_time_to_visit") val bestTimeToVisit: String?="No Data",
    @SerializedName("beach_type") val beachType: String?="No Data",
    @SerializedName("facilities")val facilities: String?="No Data",
    @SerializedName("popular_for") val popularFor: String?="No Data"
) : Parcelable
