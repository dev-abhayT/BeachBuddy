package com.example.beachbuddy

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.view.setPadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.beachbuddy.adapters.BeachCardAdapter
import com.example.beachbuddy.adapters.OnBeachClickListener
import com.example.beachbuddy.backend.BeachRepository
import com.example.beachbuddy.data.Beach
import com.example.beachbuddy.databinding.FragmentListViewBinding
import com.example.beachbuddy.interfaces.FourSquareServices
import com.example.beachbuddy.interfaces.MarineWeatherAPIService
import com.example.beachbuddy.viewModels.BeachViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class FragmentBeachList : Fragment(), OnBeachClickListener {
    private lateinit var _binding : FragmentListViewBinding
    private val binding get()= _binding!!
    private val beachViewModel : BeachViewModel by activityViewModels()
    private lateinit var adapter : BeachCardAdapter
    //private lateinit var repository: BeachRepository
    private var beaches = mutableListOf<Beach>()
    private lateinit var fusedLocClient : FusedLocationProviderClient


    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
        private val TAG = "BeachListFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.e(TAG, "ONCREATEVIEW_CALLED")
        _binding = FragmentListViewBinding.inflate(inflater, container, false)
        val view = _binding.root
        return view

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().findViewById<BottomNavigationView>(R.id.home_screen_bottom_nav)?.let{ nav ->
            binding.beachRecyclerView.post {
                val height = nav.height
                binding.beachRecyclerView.setPadding(binding.beachRecyclerView.paddingLeft,binding.beachRecyclerView.paddingTop,binding.beachRecyclerView.paddingRight,height)
            }


        }

        setupRecyclerView()
        //setupRepository()
        setupLocation()

        // Request location permission and load beaches
        requestLocationAndLoadBeaches()


    }

    private fun setupRecyclerView(){
        adapter = BeachCardAdapter(this)

        binding.beachRecyclerView.apply {
            adapter = this@FragmentBeachList.adapter
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))

        }
    }

//    private fun setupRepository(){
//        val retrofit=Retrofit.Builder()
//            .baseUrl("https://api.foursquare.com/")
//            .addConverterFactory(GsonConverterFactory.create())
//            .build()
//        val goMapsApi = retrofit.create(FourSquareServices::class.java)
//    val retrofitForMarine = Retrofit.Builder()
//        .baseUrl("https://marine-api.open-meteo.com/")
//        .addConverterFactory(GsonConverterFactory.create())
//        .build()
//
//        val marineWeatherApi = retrofitForMarine.create(MarineWeatherAPIService::class.java)
//
//
//        repository = BeachRepository(goMapsApi, BuildConfig.FSQ_API_KEY, marineWeatherApi)
//    }

    private fun setupLocation() {
        fusedLocClient = LocationServices.getFusedLocationProviderClient(requireActivity())
    }

    private fun requestLocationAndLoadBeaches() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            getCurrentLocationAndLoadBeaches()
        } else {
            requestPermissions(
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun getCurrentLocationAndLoadBeaches() {
        try {
            fusedLocClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    loadNearbyBeaches(location.latitude, location.longitude)
                } else {
                    // Use default location (Mumbai coordinates as fallback)
                    Log.e(TAG,"Using Default Location, Location Fetch Failed")
                    loadNearbyBeaches(19.07, 72.87)
                }
            }.addOnFailureListener {
                // Use default location if location fetch fails
                Log.e(TAG,"Using Default Location, Location Fetch Failed")
                loadNearbyBeaches(19.07, 72.87)
            }
        } catch (e: SecurityException) {
            // Use default location if permission is denied
            Log.e(TAG,"Using Default Location, Location Fetch Failed")
            loadNearbyBeaches(19.07, 72.87)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocationAndLoadBeaches()
            } else {
                // Use default location if permission denied
                Log.e(TAG,"Using Default Location, Location Fetch Failed")
                loadNearbyBeaches(19.07, 72.87)
            }
        }
    }

    private fun loadNearbyBeaches(latitude: Double, longitude: Double) {

        try{
            showLoading(true)
            hideError()
            beachViewModel.beachList.observe(viewLifecycleOwner) { beachList ->
                beaches.clear()
                beaches.addAll(beachList)
                adapter.submitList(beaches.toList())
                showEmptyState(beaches.isEmpty(), latitude, longitude)
            }
        } catch (e:Exception){
            showError("Failed to load beaches: ${e.message}")
        }finally {
            showLoading(false)
        }


//        lifecycleScope.launch {
//            try {
//                showLoading(true)
//                hideError()
//
//                val beachList = repository.getBeaches(latitude, longitude)
//                beaches.clear()
//                beaches.addAll(beachList)
//                Log.e(TAG,"${beaches.size} beaches found")
//                adapter.submitList(beaches.toList())
//                showEmptyState(beaches.isEmpty(), latitude, longitude)
//
//            } catch (e: Exception) {
//                showError("Failed to load beaches: ${e.message}")
//            } finally {
//                showLoading(false)
//            }
//        }
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.isVisible = show
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    private fun hideError() {
        // Hide any existing error states if needed
    }

    private fun showEmptyState(show: Boolean, latitude: Double, longitude: Double) {
        binding.emptyState.isVisible = show
        binding.nearbyText.isVisible = !show
        binding.emptyState.text="No Beaches at ${latitude},${longitude}."
    }

    override fun onBeachClick(beach: Beach) {
        val intent = Intent(requireContext(), BeachDetailsActivity::class.java)
        intent.putExtra("beach",beach)
        startActivity(intent)
    }




}