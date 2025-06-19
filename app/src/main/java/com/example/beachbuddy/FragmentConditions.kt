package com.example.beachbuddy

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.beachbuddy.backend.BeachDetailsGetter
import com.example.beachbuddy.data.SafetyStatus
import com.example.beachbuddy.databinding.FragmentConditionsBinding
import com.example.beachbuddy.viewModels.BeachViewModel
import kotlinx.coroutines.launch

class FragmentConditions : Fragment() {
    private var _binding : FragmentConditionsBinding?= null
    private val binding get() = _binding!!
    private val viewModel: BeachViewModel by activityViewModels()
//    private lateinit var singleBeach : Beach

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentConditionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.beach.observe(viewLifecycleOwner){ beach ->
            val getDetails = BeachDetailsGetter(beach)
            binding.wavePeriod.text=beach.wavePeriod.toString()+" s"
            binding.currentVelocity.text=beach.currentVelocity.toString()+" m/s"
            binding.swellHeight.text=beach.swellWaveHeight.toString()+" m"
            binding.swellPeriod.text=beach.swellWavePeriod.toString()+" s"
            binding.waveHeight.text=beach.waveHeight.toString()+" m"
            binding.seaSurfaceTemp.text=beach.seaSurfaceTemperature.toString()+" °C"
            binding.waveHeightBg.backgroundTintList=ColorStateList.valueOf(Color.parseColor(beach.variableStatus.waveHeight.bgColor))
            binding.wavePeriodBg.backgroundTintList=ColorStateList.valueOf(Color.parseColor(beach.variableStatus.wavePeriod.bgColor))
            binding.swellHeightBg.backgroundTintList=ColorStateList.valueOf(Color.parseColor(beach.variableStatus.swellWaveHeight.bgColor))
            binding.swellPeriodBg.backgroundTintList=ColorStateList.valueOf(Color.parseColor(beach.variableStatus.swellWavePeriod.bgColor))
            binding.sstBg.backgroundTintList=ColorStateList.valueOf(Color.parseColor(beach.variableStatus.seaSurfaceTemperature.bgColor))
            binding.currentVelocityBg.backgroundTintList=ColorStateList.valueOf(Color.parseColor(beach.variableStatus.oceanCurrentVelocity.bgColor))

            lifecycleScope.launch {
                try {
                    showLoading(true)
                    showError("", false)
                    showDetails(false)
                    if(beach.safetyStatus == SafetyStatus.SAFE){
                        binding.currentStatusImage.setImageResource(R.drawable.ic_safe)
                    }
                    binding.currentStatusTips.text=beach.tip
//                    binding.currentStatusBg.backgroundTintList= ColorStateList.valueOf(Color.parseColor(beach.safetyStatus.bgColor))
//                    binding.beachAdvisoryText.setTextColor(ColorStateList.valueOf(Color.parseColor(beach.safetyStatus.color)))
//                    binding.currentStatusTips.setTextColor(ColorStateList.valueOf(Color.parseColor(beach.safetyStatus.color)))
//                    binding.currentStatusImage.imageTintList = ColorStateList.valueOf(Color.parseColor(beach.safetyStatus.color))
                    val getWeather = getDetails.getWeatherForecast()
                    binding.temperature.text=getWeather.currentForecast.temperature.toString()+"°C"
                    binding.humidity.text=getWeather.currentForecast.humidity.toString()+"%"
                    binding.windSpeed.text=getWeather.currentForecast.windSpeed.toString()+" km/h"
                    binding.precipitation.text=getWeather.currentForecast.precipitation.toString()+" mm"


                    binding.sunrise.text=getDetails.convertTime(getWeather.dailyForecast.sunrise[0])
                    binding.sunset.text=getDetails.convertTime(getWeather.dailyForecast.sunset[0])

                    val uvClassify = getDetails.classifyUV(getWeather.dailyForecast.uvIndexMax[0])
//                    val highTideTime = getDetails.getTideInfo("High Tide")
//                    binding.nextHighTide.text = highTideTime
//                    binding.nextHighTideTip.text = getDetails.getTimeDifferenceFromNow(highTideTime)
//                    binding.nextLowTide.text = getDetails.getTideInfo("Low Tide")
                    binding.uvCardBg.backgroundTintList= ColorStateList.valueOf(Color.parseColor(uvClassify.color))

                    binding.uvStatus.text=uvClassify.displayName
                    binding.uvIndexTip.text=uvClassify.tip



                    showDetails(true)

                } catch (e: Exception) {
                    showError("${e.message}", true)
                    showDetails(false)
                } finally {
                    showLoading(false)
                }


            }


        }
    }

    fun showLoading(isLoading: Boolean){
        binding.loadingDetails.isVisible = isLoading
    }

    fun showError(text:String, show: Boolean){
        binding.errorMessage.text = text
        binding.errorMessage.isVisible = show
    }

    fun showDetails(show: Boolean){
        binding.beachDetailsContainer.isVisible = show
    }
}

