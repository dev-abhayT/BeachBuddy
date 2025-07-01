package com.example.beachbuddy

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.beachbuddy.backend.BeachDetailsGetter
import com.example.beachbuddy.data.BeachQuickFacts
import com.example.beachbuddy.data.SafetyStatus
import com.example.beachbuddy.databinding.FragmentOverviewBinding
import com.example.beachbuddy.viewModels.BeachViewModel
import com.google.gson.Gson
import kotlinx.coroutines.launch

class FragmentOverview : Fragment() {
    private lateinit var _binding : FragmentOverviewBinding
    private val binding get() = _binding!!
    private val viewModel: BeachViewModel by activityViewModels()
    companion object{
        const val TAG = "FragmentOverview"
    }

//    private lateinit var singleBeach : Beach

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentOverviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.beach.observe(viewLifecycleOwner){ beach ->
            lifecycleScope.launch {
                try{
                    showLoading(true)
                    showError("",false)
                    showDetails(false)
                    val getDetails = BeachDetailsGetter(beach)
                    val quickFacts = getDetails.getQuickFacts()
                    val extractedJson = extractJsonBlock(quickFacts)
                    Log.e(TAG,extractedJson)
                    val gson = Gson()
                    val json = gson.fromJson(extractedJson, BeachQuickFacts::class.java)
                    binding.bestTime.text = json.bestTimeToVisit
                    binding.beachType.text = json.beachType
                    binding.popularFor.text = json.popularFor
                    binding.activitiesAvailable.text=json.facilities
                    binding.safetyTips.text=getDetails.getSafetyTips()
                    binding.beachShortDescription.text = getDetails.getDescription()
                    showDetails(true)
                }catch(e: Exception){
                    showError("${e.message}", true)
                    showDetails(false)
                }finally {
                    showLoading(false)
                }
            }

            binding.currentStatusBg.backgroundTintList= ColorStateList.valueOf(Color.parseColor(beach.safetyStatus.bgColor))
            binding.safetyReasonText.text=beach.safetyReason
            binding.safetyReasonText.setTextColor(Color.parseColor(beach.safetyStatus.color))
            binding.beachConditionTip.setTextColor(Color.parseColor(beach.safetyStatus.color))
            binding.conditionDrawable.imageTintList = ColorStateList.valueOf(Color.parseColor(beach.safetyStatus.color))

            binding.beachWalkingTip.text = beach.beachActivity.walking.tip
            binding.beachSwimmingTip.text = beach.beachActivity.swimming.tip
            binding.beachVolleyballTip.text = beach.beachActivity.volleyball.tip
            binding.beachWalkingStatus.text = beach.beachActivity.walking.status.label
            binding.beachSwimmingStatus.text = beach.beachActivity.swimming.status.label
            binding.beachVolleyballStatus.text = beach.beachActivity.volleyball.status.label
//            binding.beachWalkingStatusBg.backgroundTintList= ColorStateList.valueOf(Color.parseColor(beach.beachActivity.walking.status.color))
//            binding.beachSwimmingStatusBg.backgroundTintList= ColorStateList.valueOf(Color.parseColor(beach.beachActivity.swimming.status.color))
//            binding.beachVolleyballStatusBg.backgroundTintList= ColorStateList.valueOf(Color.parseColor(beach.beachActivity.volleyball.status.color))
            binding.beachSwimmingStatus.setTextColor(ColorStateList.valueOf(Color.parseColor(beach.beachActivity.swimming.status.color)))
            binding.beachWalkingStatus.setTextColor(ColorStateList.valueOf(Color.parseColor(beach.beachActivity.walking.status.color)))
            binding.beachVolleyballStatus.setTextColor(ColorStateList.valueOf(Color.parseColor(beach.beachActivity.volleyball.status.color)))



                if(beach.safetyStatus==SafetyStatus.SAFE){
                    binding.conditionDrawable.setImageResource(R.drawable.ic_safe)
                    binding.beachConditionTip.text = "Beach is safe to sail in!"


                }
                else if(beach.safetyStatus==SafetyStatus.DANGEROUS){
                    binding.beachConditionTip.text="Advisable Not to visit this beach right now."
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
    fun extractJsonBlock(response: String): String {
        val start = response.indexOf('{')
        val end = response.lastIndexOf('}')
        return if (start != -1 && end != -1 && end > start) {
            response.substring(start, end + 1)
        } else ""
    }





}