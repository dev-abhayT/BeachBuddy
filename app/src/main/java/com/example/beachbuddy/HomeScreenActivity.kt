package com.example.beachbuddy

import android.content.Intent
import android.os.Bundle
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.work.impl.utils.tryDelegateRemoteListenableWorker
import com.example.beachbuddy.adapters.MapPagerAdapter
import com.example.beachbuddy.data.fallbackLocations
import com.example.beachbuddy.data.selectedIndex
import com.example.beachbuddy.databinding.ActivityHomeScreenBinding
import com.example.beachbuddy.viewModels.FallbackViewModel
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth

class HomeScreenActivity : AppCompatActivity() {


    private lateinit var binding : ActivityHomeScreenBinding
    private val filterSharedViewModel : FallbackViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val viewPager = binding.mapViewPager
        val tabLayout = binding.homeTabLayout
        val auth = FirebaseAuth.getInstance()
//        val bottomNav = binding.homeScreenBottomNav
//       bottomNav.selectedItemId = R.id.explore
//        bottomNav.setOnItemSelectedListener {
//            when (it.itemId) {
//                R.id.profile -> {
//                        startActivity(Intent(this, ProfileActivity::class.java))
//                        true
//                }
//                else -> false
//            }
//        }


        binding.filterBtn.setOnClickListener {
            try {
                val layout = layoutInflater.inflate(R.layout.location_filter_dialog,null)
                val radioGroup = layout.findViewById<RadioGroup>(R.id.list_fallback_locations)

                fallbackLocations.forEachIndexed { index, location ->
                    val radioButton = RadioButton(this)
                    radioButton.text=location.name
                    radioButton.id = index
                    radioGroup.addView(radioButton)

                    if (index == selectedIndex) {
                        radioButton.isChecked = true
                    }

                }

                val dialog = AlertDialog.Builder(this)
                    .setView(layout)
                    .setPositiveButton("Select"){ _,_ ->
                        val selectedRadioButtonId = radioGroup.checkedRadioButtonId
                        val selectedRadioButton = radioGroup.findViewById<RadioButton>(selectedRadioButtonId)
                        selectedIndex = selectedRadioButton.id
                        filterSharedViewModel.setLocation(fallbackLocations[selectedIndex])
                    }
                    .setNegativeButton("Cancel",null)

                dialog.show()
            }catch (e:Exception){
                Toast.makeText(this, "${e.message}",Toast.LENGTH_SHORT).show()
            }

        }
        binding.logoutHomescreen.setOnClickListener {
            try{
                val logout_layout = layoutInflater.inflate(R.layout.logout_dialog,null)
                val dialog = AlertDialog.Builder(this)
                    .setView(logout_layout)
                    .setPositiveButton("Yes"){ _,_ ->
                        auth.signOut()
                        val intent = Intent(this, AuthActivity::class.java)
                        val sharedPref = getSharedPreferences("CheckLogin", MODE_PRIVATE)
                        sharedPref.edit().putBoolean("isLoggedIn",false).apply()
                        val emailSharedPrefs = getSharedPreferences("UserEmail", MODE_PRIVATE)
                        emailSharedPrefs.edit().remove("userEmail").apply()
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()}
                    .setNegativeButton("No",null)

                dialog.show()

            }catch (e:Exception){
                Toast.makeText(this, "${e.message}",Toast.LENGTH_SHORT).show()
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