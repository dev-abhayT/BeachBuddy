package com.example.beachbuddy

import android.content.Intent
import android.credentials.ClearCredentialStateRequest
import android.credentials.CredentialManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.credentials.exceptions.ClearCredentialException
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.beachbuddy.data.Person
import com.example.beachbuddy.databinding.ActivityProfileBinding
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ProfileActivity : AppCompatActivity() {
    private lateinit var binding : ActivityProfileBinding
    private lateinit var auth : FirebaseAuth

    companion object{
        private const val TAG = "ProfileActivity"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
       val emailSharedPrefs = getSharedPreferences("UserEmail", MODE_PRIVATE)
        val email = emailSharedPrefs.getString("userEmail","")

//        val bottomNav = binding.profileScreenBottomNav
//        bottomNav.selectedItemId=R.id.profile
//        bottomNav.setOnItemSelectedListener {
//            when(it.itemId){
//                R.id.explore -> {
//                    finish()
//                    true
//                }
//                else -> false
//            }}

        lifecycleScope.launch {
           try{
               showLoading(true)
               showMainPage(false)
               val person = getPersonData(email)
               Glide.with(this@ProfileActivity).load(person.photoUrl).placeholder(R.drawable.activity_splash_background).error(R.drawable.app_logo).into(binding.userProfileImage)
               binding.userName.text = person.firstName
               binding.userEmail.text = person.email
           }catch(e:Exception){

           }finally {
               showLoading(false)
               showMainPage(true)
           }


        }

binding.btnSignOut.setOnClickListener {
    auth.signOut()
    val intent = Intent(this, AuthActivity::class.java)
    val sharedPref = getSharedPreferences("CheckLogin", MODE_PRIVATE)
    sharedPref.edit().putBoolean("isLoggedIn",false).apply()
    emailSharedPrefs.edit().remove("userEmail").apply()
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    startActivity(intent)
    finish()
}
        binding.btnBack.setOnClickListener {
            finish()
        }

    }

    suspend fun getPersonData(email : String?) : Person {
        return try{
            val snapshot = Firebase.firestore.collection("persons").document(email!!).get().await()
            snapshot.toObject(Person::class.java)!!
        }catch (e : Exception){
            Log.e(TAG,"Error getting person data : ${e.message}")
            Person()
        }
    }

    fun showLoading(show : Boolean){
        binding.loadingOverlay.isVisible = show
    }
    fun showMainPage(visible : Boolean){
        binding.mainDetailsView.isVisible = visible
    }
}