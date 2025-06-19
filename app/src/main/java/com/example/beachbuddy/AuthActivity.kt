package com.example.beachbuddy

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

import com.example.beachbuddy.adapters.ViewPagerAdapter
import com.example.beachbuddy.databinding.ActivityAuthBinding
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import java.util.prefs.Preferences

class AuthActivity : AppCompatActivity() {

    private lateinit var binding : ActivityAuthBinding
    private val tabItems = listOf("Login","Sign Up")
    private val auth by lazy { FirebaseAuth.getInstance() }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (auth.currentUser != null){
            val intent = Intent(this,HomeScreenActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        val viewPager = binding.viewPager
        val tabLayout = binding.tabSelector

        val fragments = listOf(FragmentLogin(),FragmentSignUp())
        val adapter = ViewPagerAdapter(this,fragments)
        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout,viewPager) { tab, position ->
            tab.text = tabItems[position]
        }.attach()

        tabLayout.addOnTabSelectedListener(object : OnTabSelectedListener{
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (tab != null) {
                    if(tab.position == 0){
                        binding.welcomeText.text = "Welcome Back"
                        binding.welcomeSubtitleText.text = "Sign in to access BeachBuddy"
                        viewPager.currentItem = 0
                    } else{
                        binding.welcomeText.text = "Welcome"
                        binding.welcomeSubtitleText.text = "Create an account to access BeachBuddy"
                        viewPager.currentItem = 1
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}

            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

    }
}