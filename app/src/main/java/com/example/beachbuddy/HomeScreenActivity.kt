package com.example.beachbuddy

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.beachbuddy.adapters.MapPagerAdapter
import com.example.beachbuddy.databinding.ActivityHomeScreenBinding
import com.google.android.material.tabs.TabLayoutMediator

class HomeScreenActivity : AppCompatActivity() {

    private lateinit var binding : ActivityHomeScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val viewPager = binding.mapViewPager
        val tabLayout = binding.homeTabLayout
        val bottomNav = binding.homeScreenBottomNav
       bottomNav.selectedItemId = R.id.explore
        bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.profile -> {
                        startActivity(Intent(this, ProfileActivity::class.java))
                        true
                }
                else -> false
            }
        }



//        binding.profileNavigator.setOnClickListener {
//            val intent = Intent(this, ProfileActivity::class.java)
//            startActivity(intent)
//        }
        val fragments = listOf(FragmentMapContainer(),FragmentBeachList())
        val adapter = MapPagerAdapter(this,fragments)
        viewPager.adapter = adapter
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Map View"
                1 -> "List View"
                else -> ""
            }
        }.attach()
    }
}