package com.example.beachbuddy

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.beachbuddy.databinding.ActivitySplashBinding
import com.google.firebase.auth.FirebaseAuth
import java.util.logging.Handler

class SplashActivity : AppCompatActivity() {
    private lateinit var binding : ActivitySplashBinding

    private val auth by lazy { FirebaseAuth.getInstance() }
    companion object{
        private const val SPLASH_DELAY : Long = 2000
        private const val TAG = "SplashActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        val sharedPref = getSharedPreferences("CheckLogin", Context.MODE_PRIVATE)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val runnable = Runnable {
            val isLoggedin = sharedPref.getBoolean("isLoggedIn",false)
            Log.e(TAG,"$isLoggedin")
            if(auth.currentUser != null  || isLoggedin){
                val intent = Intent(this,HomeScreenActivity::class.java)
                startActivity(intent)

            }
            else{
                val intent = Intent(this,AuthActivity::class.java)
                startActivity(intent)
            }
            finishAffinity()
        }
        val handler = android.os.Handler()
        handler.postDelayed(runnable, SPLASH_DELAY)
    }
}