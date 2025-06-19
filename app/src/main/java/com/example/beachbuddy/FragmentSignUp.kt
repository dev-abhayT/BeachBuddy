package com.example.beachbuddy

import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.credentials.GetCredentialException
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat.finishAffinity
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialResponse
import androidx.fragment.app.Fragment
import com.example.beachbuddy.FragmentLogin.Companion
import com.example.beachbuddy.data.Person
import com.example.beachbuddy.databinding.FragmentLoginBinding
import com.example.beachbuddy.databinding.FragmentSignupBinding
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class FragmentSignUp : Fragment() {
    private val auth by lazy {FirebaseAuth.getInstance()}
    private var _binding : FragmentSignupBinding? = null
    private val binding get() = _binding!!
    private val personRef = Firebase.firestore.collection("persons")

    companion object{
        private const val TAG = "FragmentSignUp"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignupBinding.inflate(inflater,container,false)
        val view = binding.root
        return view
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.createBtn.setOnClickListener {
            val name = binding.nameEt.text.toString().trim()
            val emailText = binding.emailEt.text.toString().trim()
            val passText = binding.passEt.text.toString().trim()
            val confirmPassText = binding.confirmpassEt.text.toString().trim()


            if(name.isEmpty() || emailText.isEmpty() || passText.isEmpty() || confirmPassText.isEmpty()){
                Toast.makeText(requireContext(),"Given Fields cannot be empty!",Toast.LENGTH_SHORT).show()
            }
            else {
                if (passText == confirmPassText && Patterns.EMAIL_ADDRESS.matcher(emailText).matches()) {
                    auth.createUserWithEmailAndPassword(emailText, passText).addOnCompleteListener {
                        if(it.isSuccessful){
                            val emailSharedPrefs = activity?.getSharedPreferences("UserEmail", Context.MODE_PRIVATE)
                            emailSharedPrefs?.edit()?.putString("userEmail",emailText)?.apply()
                            Toast.makeText(requireContext(),"Account Created Successfully!",Toast.LENGTH_SHORT).show()
                            val person = Person(name,emailText)
                            savePersonwithData(person,emailText)
                            val sharedPref = activity?.getSharedPreferences("CheckLogin", Context.MODE_PRIVATE)
                            sharedPref?.edit()?.putBoolean("isLoggedIn",true)?.apply()
                            val isLoggedin = sharedPref?.getBoolean("isLoggedIn",false)
                            Log.e(TAG,"MANUAL SIGN UP: $isLoggedin")
                            val intent = Intent(requireContext(),HomeScreenActivity::class.java)
                            intent.flags = FLAG_ACTIVITY_NEW_TASK or FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                        }
                        else{
                            Toast.makeText(requireContext(),"Account Creation Failed : ${it.exception?.message}",Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                else if (!Patterns.EMAIL_ADDRESS.matcher(emailText).matches()){
                    Toast.makeText(requireContext(),"Invalid Email Address!",Toast.LENGTH_SHORT).show()
                }
                else{
                    Toast.makeText(requireContext(),"Passwords do not match!",Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.btnGoogle.setOnClickListener {
            val credentialManager = androidx.credentials.CredentialManager.create(requireContext())
            val gso = GetSignInWithGoogleOption.Builder(getString(R.string.WEB_CLIENT_ID))
                .build()

            val request: androidx.credentials.GetCredentialRequest = androidx.credentials.GetCredentialRequest.Builder()
                .addCredentialOption(gso)
                .build()

            CoroutineScope(Dispatchers.Main).launch {
                try {
                    val result = credentialManager.getCredential(
                        request = request,
                        context = requireContext(),
                    )
                    handleSignIn(result)
                } catch (e: GetCredentialException) {
                    Toast.makeText(requireContext(),"${e.message}", Toast.LENGTH_SHORT).show()
                    Log.e(TAG, "Error getting credential", e)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun savePersonwithData(person : Person, email : String) = CoroutineScope(Dispatchers.IO).launch {
        try{
            personRef.document(email).set(person).await()
            withContext(Dispatchers.Main){
                Toast.makeText(requireContext(),"First and Last Name Saved",Toast.LENGTH_SHORT).show()
            }
    } catch (e : Exception){
        withContext(Dispatchers.Main) {
            Toast.makeText(requireContext(),"Error : ${e.message}",Toast.LENGTH_SHORT).show()
        }

        }    }

    fun handleSignIn(result: GetCredentialResponse) {
        val credential = result.credential

        when (credential) {
            is CustomCredential -> {
                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    try {
                        val googleIdTokenCredential = GoogleIdTokenCredential
                            .createFrom(credential.data)

                        val idToken = googleIdTokenCredential.idToken
                        val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)

                        auth.signInWithCredential(firebaseCredential)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    val isNewUser =
                                        task.result?.additionalUserInfo?.isNewUser == true

                                    val name = googleIdTokenCredential.displayName?: ""
                                    val userEmail = googleIdTokenCredential.id?: ""
                                    val photoUrl = googleIdTokenCredential.profilePictureUri?.toString()?: ""
                                    val person = Person(name, userEmail, photoUrl)
                                    val emailSharedPrefs = activity?.getSharedPreferences("UserEmail", Context.MODE_PRIVATE)

                                    if (isNewUser) {

                                        emailSharedPrefs?.edit()?.putString("userEmail",userEmail)?.apply()
                                        savePersonwithData(person, userEmail)
                                        Toast.makeText(requireContext(), "Signed Up with Google!", Toast.LENGTH_SHORT).show()
                                        val sharedPref = activity?.getSharedPreferences("CheckLogin", Context.MODE_PRIVATE)

                                        sharedPref?.edit()?.putBoolean("isLoggedIn",true)?.apply()
                                        val isLoggedin = sharedPref?.getBoolean("isLoggedIn",false)
                                        Log.e(TAG,"Google Signup : $isLoggedin")
                                    } else {
                                        emailSharedPrefs?.edit()?.putString("userEmail",userEmail)?.apply()
                                        Toast.makeText(requireContext(), "Signed In with Google!", Toast.LENGTH_SHORT).show()
                                        val sharedPref = activity?.getSharedPreferences("CheckLogin", Context.MODE_PRIVATE)

                                        sharedPref?.edit()?.putBoolean("isLoggedIn",true)?.apply()
                                        val isLoggedin = sharedPref?.getBoolean("isLoggedIn",false)
                                        Log.e(TAG,"Google SignIn : $isLoggedin")

                                    }
                                    val intent = Intent(requireContext(),HomeScreenActivity::class.java)
                                    intent.flags = FLAG_ACTIVITY_NEW_TASK or FLAG_ACTIVITY_CLEAR_TASK
                                    startActivity(intent)
                                } else {
                                    Toast.makeText(requireContext(), "Google Sign-In failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                                }
                            }

                    } catch (e: GoogleIdTokenParsingException) {
                        Log.e(TAG, "Invalid Google ID token", e)
                    }
                }
            }

            else -> {
                Log.e(TAG, "Unexpected type of credential")
            }
        }
    }

}