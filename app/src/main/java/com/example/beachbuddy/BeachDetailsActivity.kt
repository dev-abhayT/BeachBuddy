package com.example.beachbuddy

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.beachbuddy.adapters.ViewPagerAdapter
import com.example.beachbuddy.data.Beach
import com.example.beachbuddy.databinding.ActivityBeachDetailsBinding
import com.example.beachbuddy.viewModels.BeachViewModel
import com.google.android.material.tabs.TabLayoutMediator

class BeachDetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBeachDetailsBinding
    private val viewModel: BeachViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityBeachDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val beach = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("beach", Beach::class.java)
        } else {
            intent.getParcelableExtra("beach")
        }

        beach?.let{viewModel.setBeach(it)}

        val viewPager = binding.beachDetailsViewPager
        val tabLayout = binding.beachDetailsTabLayout
        val fragments = listOf(FragmentOverview(), FragmentConditions())
        val adapter = ViewPagerAdapter(this, fragments)
        viewPager.adapter = adapter
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Overview"
                1 -> "Conditions"
                else -> ""
            }
        }.attach()


        try{
            binding.tvBeachTitle.text=beach?.name
            binding.beachSafetyStatus.text=beach?.safetyStatus?.displayName
            binding.tvDistanceText.text="${beach?.address}"
            Glide.with(this)
                .load(beach?.photoUrl)
                .placeholder(R.drawable.ic_google)
                .error(R.drawable.activity_splash_background)
                .into(binding.ivBeachImage)
            val color = Color.parseColor(beach?.safetyStatus?.color)
            binding.beachSafetyStatus.setTextColor(ColorStateList.valueOf(color))

            val bgColor = Color.parseColor(beach?.safetyStatus?.bgColor)

//            binding.safetyStatusDetailBackground.backgroundTintList= ColorStateList.valueOf(bgColor)
//            binding.tvSafetyStatusDetail.setTextColor(ColorStateList.valueOf(Color.parseColor(beach?.safetyStatus?.color)))
//            binding.tvSafetyStatusDetail.text = beach?.safetyReason

        }catch (e:NullPointerException){
            Toast.makeText(this,"Error Loading Beach! ${e.message}",Toast.LENGTH_SHORT).show()
        }

        binding.backButton.setOnClickListener {
            finish()

        }

    }
}