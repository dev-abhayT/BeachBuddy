package com.example.beachbuddy.backend

import android.os.Parcelable
import com.example.beachbuddy.data.Beach
import com.example.beachbuddy.data.SafetyStatus
import kotlinx.parcelize.Parcelize


class BeachClassifier(var waveHeight : Double, var wavePeriod : Double, var windWaveHeight : Double, var windWavePeriod : Double, var swellWaveHeight : Double, var swellWavePeriod : Double, var seaSurfaceTemperature : Double, var oceanCurrentVelocity : Double){
    fun classifyBeach(): BeachReport {
        val summaryParts = mutableListOf<String>()
        val tips = mutableListOf<String>()

        val currentVelocityMps = oceanCurrentVelocity / 3.6


        val status = when {
            waveHeight > 2.5 || swellWaveHeight > 2.0 -> {
                summaryParts += "High waves or strong swells present"
                tips += "Avoid swimming today — waves may be too rough"
                SafetyStatus.DANGEROUS
            }
            currentVelocityMps > 1.0 -> {
                summaryParts += "Strong ocean currents are present"
                tips += "Avoid deep swims — currents could drag swimmers"
                SafetyStatus.DANGEROUS
            }
            windWaveHeight > 1.2 || windWavePeriod < 4.0 -> {
                summaryParts += "Choppy wind waves expected"
                tips += "Not ideal for beginner swimmers"
                SafetyStatus.CAUTION
            }
            wavePeriod < 5.0 -> {
                summaryParts += "Short wave periods indicate choppy surf"
                tips += "\nWaves may be rough and frequent"
                SafetyStatus.CAUTION
            }
            seaSurfaceTemperature !in 18.0..32.0 -> {
                summaryParts += "Uncomfortable water temperature (${seaSurfaceTemperature}°C)"
                tips += "\nWater may be too cold or hot for comfort"
                SafetyStatus.CAUTION
            }
            else -> {
                summaryParts += "Conditions appear calm and swimmable"
                tips += "\n Ideal for swimming and relaxation"
                SafetyStatus.SAFE
            }
        }


        when {
            waveHeight > 2.5 -> summaryParts += "Wave height exceeds 2.5 meters"
            waveHeight > 1.5 -> summaryParts += "Moderate wave activity around ${"%.1f".format(waveHeight)} meters"
            else -> summaryParts += "Calm waves (${waveHeight} m)"
        }

        if (swellWavePeriod > 10.0) {
            summaryParts += "Long-period swell indicates strong surf"
            tips += "\n Waves may be powerful even if infrequent"
        }

        if (windWavePeriod < 4.0 && windWaveHeight > 1.2) {
            summaryParts += "Short, choppy wind-driven waves"
        }

        if (oceanCurrentVelocity > 4.0) {
            tips += "\n Rip current risk — avoid far swims"
        }

        if (seaSurfaceTemperature in 25.0..30.0) {
            tips += "\n Perfect water temperature — no wetsuit needed"
        }

        val waveHeightStatus = when {
            waveHeight > 2.5 -> ActivitySafetyStatus.UNSAFE
            waveHeight > 1.5 -> ActivitySafetyStatus.CAUTION
            else -> ActivitySafetyStatus.SAFE
        }

        val swellHeightStatus = when {
            swellWaveHeight > 2.0 -> ActivitySafetyStatus.UNSAFE
            swellWaveHeight > 1.0 -> ActivitySafetyStatus.CAUTION
            else -> ActivitySafetyStatus.SAFE
        }

        val wavePeriodStatus = when {
            wavePeriod < 5.0 -> ActivitySafetyStatus.CAUTION
            else -> ActivitySafetyStatus.SAFE
        }

        val swellPeriodStatus = when {
            swellWavePeriod > 10.0 -> ActivitySafetyStatus.CAUTION
            else -> ActivitySafetyStatus.SAFE
        }

        val currentStatus = when {
            currentVelocityMps > 1.0 -> ActivitySafetyStatus.UNSAFE
            currentVelocityMps > 0.6 -> ActivitySafetyStatus.CAUTION
            else -> ActivitySafetyStatus.SAFE
        }

        val sstStatus = when (seaSurfaceTemperature) {
            in 25.0..30.0 -> ActivitySafetyStatus.SAFE
            in 18.0..32.0 -> ActivitySafetyStatus.CAUTION
            else -> ActivitySafetyStatus.UNSAFE
        }
        val variableStatus = VariableSafetyStatus(waveHeightStatus, wavePeriodStatus, swellHeightStatus, swellPeriodStatus, sstStatus, currentStatus)
        val summary = summaryParts.joinToString(". ") + "."
        val tip = tips.distinct().joinToString(". ")

        val volleyballTip = when (status) {
            SafetyStatus.SAFE -> "Perfect weather for a beach volleyball match!"
            SafetyStatus.CAUTION -> "Windy but playable — secure lightweight items."
            SafetyStatus.DANGEROUS -> "Conditions may be too harsh for safe gameplay."
        }

        val swimmingTip = when (status) {
            SafetyStatus.SAFE -> "Great time to swim — enjoy the calm waters!"
            SafetyStatus.CAUTION -> "Swim with caution — waves or currents may be tricky."
            SafetyStatus.DANGEROUS -> "Swimming not recommended due to rough conditions."
        }

        val walkingTip = when (status) {
            SafetyStatus.SAFE -> "Ideal for a peaceful beach walk."
            SafetyStatus.CAUTION -> "Some wind or heat — take precautions."
            SafetyStatus.DANGEROUS -> "Walking may be uncomfortable or unsafe."
        }
        val walkingStatus = getActivityStatus(status, Activity.WALKING)
        val volleyballStatus = getActivityStatus(status, Activity.VOLLEYBALL)
        val swimmingStatus = getActivityStatus(status, Activity.SWIMMING)

        val beachActivity = BeachActivity(
            walking = Walking(walkingStatus, walkingTip),
            volleyball = Volleyball(volleyballStatus, volleyballTip),
            swimming = Swimming(swimmingStatus, swimmingTip)
        )


        return BeachReport(status, summary, tip, beachActivity, variableStatus)
    }

    fun getActivityStatus(safetyStatus: SafetyStatus, activity: Activity): ActivitySafetyStatus {
        return when (safetyStatus) {
            SafetyStatus.SAFE -> ActivitySafetyStatus.SAFE

            SafetyStatus.CAUTION -> when (activity) {
                Activity.SWIMMING -> ActivitySafetyStatus.CAUTION
                Activity.VOLLEYBALL -> ActivitySafetyStatus.SAFE
                Activity.WALKING -> ActivitySafetyStatus.SAFE
            }

            SafetyStatus.DANGEROUS -> when (activity) {
                Activity.SWIMMING -> ActivitySafetyStatus.UNSAFE
                Activity.VOLLEYBALL -> ActivitySafetyStatus.CAUTION
                Activity.WALKING -> ActivitySafetyStatus.CAUTION
            }
        }
    }


}
@Parcelize
data class BeachReport(
    val safetyStatus: SafetyStatus,
    val summary: String,
    val tip: String,
    val beachActivity : BeachActivity,
    val variableStatus : VariableSafetyStatus
) : Parcelable

@Parcelize
data class BeachActivity(
    val walking : Walking,
    val volleyball : Volleyball,
    val swimming : Swimming
) : Parcelable

@Parcelize
data class VariableSafetyStatus(
    val waveHeight: ActivitySafetyStatus,
    val wavePeriod: ActivitySafetyStatus,
    val swellWaveHeight: ActivitySafetyStatus,
    val swellWavePeriod: ActivitySafetyStatus,
    val seaSurfaceTemperature: ActivitySafetyStatus,
    val oceanCurrentVelocity: ActivitySafetyStatus
) : Parcelable

@Parcelize
data class Walking(
    val status: ActivitySafetyStatus,
    val tip: String
) : Parcelable

@Parcelize
data class Volleyball(
    val status: ActivitySafetyStatus,
    val tip: String
) : Parcelable

@Parcelize
data class Swimming(
    val status: ActivitySafetyStatus,
    val tip: String
) : Parcelable


enum class ActivitySafetyStatus(val label: String, val color: String, val bgColor : String) {
    SAFE("Safe", "#10B981", "#90EE90"),       // Green
    CAUTION("Caution", "#F59E0B", "#FFD580"), // Yellow
    UNSAFE("Unsafe", "#EF4444", "#FFC1C3")    // Red
}

enum class Activity(val label: String, val color: String) {
    SWIMMING("Swimming", "#10B981"),
    VOLLEYBALL("Volleyball", "#F59E0B"),
    WALKING("Walking", "#EF4444")
}


