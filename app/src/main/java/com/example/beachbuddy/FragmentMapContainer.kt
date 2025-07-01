package com.example.beachbuddy

import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log

import android.view.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.example.beachbuddy.backend.BeachRepository
import com.example.beachbuddy.data.Beach
import com.example.beachbuddy.data.room.BeachEntity
import com.example.beachbuddy.data.room.BeachLocalDB
import com.example.beachbuddy.databinding.FragmentMapBinding
import com.example.beachbuddy.interfaces.FourSquareServices
import com.example.beachbuddy.interfaces.MarineWeatherAPIService
import com.example.beachbuddy.viewModels.BeachViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
//import com.google.android.libraries.maps.CameraUpdateFactory
//import com.google.android.libraries.maps.GoogleMap
//import com.google.android.libraries.maps.OnMapReadyCallback
//import com.google.android.libraries.maps.SupportMapFragment
//import com.google.android.libraries.maps.model.LatLng
//import com.google.android.libraries.maps.model.BitmapDescriptorFactory
//import com.google.android.libraries.maps.model.PinConfig
//import com.google.android.libraries.maps.model.AdvancedMarkerOptions


import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class FragmentMapContainer : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!
//    private lateinit var firestore : FirebaseFirestore
    private val beachViewModel: BeachViewModel by activityViewModels()
    private lateinit var googleMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var repository: BeachRepository
    private val beaches = mutableListOf<Beach>()

//    private val defaultLocation = LatLng(19.19, 72.86)

    companion object {
        private const val TAG = "FRAGMENT_MAP_CONTAINER"
    }

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            getCurrentLocationAndFetchBeaches()
        } else {
            Toast.makeText(requireContext(), "Location Permission Denied", Toast.LENGTH_SHORT).show()
            Log.e(TAG,"Location Permission Denied")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        val emailSharedPrefs = requireActivity().getSharedPreferences("UserEmail", MODE_PRIVATE)
//        val email = emailSharedPrefs.getString("userEmail","")
//
//        firestore = FirebaseFirestore.getInstance()


        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        val mapFragment = SupportMapFragment.newInstance()
        childFragmentManager.beginTransaction()
            .replace(R.id.map_container, mapFragment)
            .commit()
        mapFragment.getMapAsync(this)

        requireActivity().findViewById<BottomNavigationView>(R.id.home_screen_bottom_nav)?.let{ nav ->
            binding.parentLayoutMapFragment.post {
                val height = nav.height
                binding.parentLayoutMapFragment.setPadding(binding.parentLayoutMapFragment.paddingLeft,binding.parentLayoutMapFragment.paddingTop,binding.parentLayoutMapFragment.paddingRight,height)
            }


        }

        binding.centerMyLoc.setOnClickListener {
            centerMyloc()
        }
    }

    private fun centerMyloc() {
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener {
                if (it != null) {
                    val latLng = LatLng(it.latitude, it.longitude)
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12f))
                }
            }
        }catch(e:SecurityException){
            Log.e(TAG,"Center My Location Clicked But Failed!")
            Toast.makeText(requireContext(),"Security Exception Caught! ${e.message}",Toast.LENGTH_SHORT).show()
        }

    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            googleMap.isMyLocationEnabled = true
            getCurrentLocationAndFetchBeaches()
            Toast.makeText(requireContext(), "Location Permission Granted", Toast.LENGTH_SHORT).show()
        } else {
            locationPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun getCurrentLocationAndFetchBeaches() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            locationPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                val latLng = LatLng(location.latitude, location.longitude)
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12f))
                fetchNearbyBeaches(location.latitude, location.longitude)
            } else {
                Toast.makeText(requireContext(), "Couldn't access location. Showing default.", Toast.LENGTH_SHORT).show()
                Log.e(TAG, "Couldn't access location.")
            }
        }.addOnFailureListener {
            Log.e(TAG, "Location failure: ${it.message}")
            Toast.makeText(requireContext(), "Failed to get location", Toast.LENGTH_SHORT).show()
            Log.e(TAG,"Failed to get location")
        }
    }

//    private fun moveToDefaultLocation() {
//        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 12f))
//        fetchNearbyBeaches(defaultLocation.latitude, defaultLocation.longitude)
//    }

    private fun fetchNearbyBeaches(latitude: Double, longitude: Double) {
        // Init Retrofit only once if needed
        val retrofitGoMaps = Retrofit.Builder()
            .baseUrl("https://places-api.foursquare.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val goMapsApi = retrofitGoMaps.create(FourSquareServices::class.java)

        val retrofitMarine = Retrofit.Builder()
            .baseUrl("https://marine-api.open-meteo.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val marineWeatherApi = retrofitMarine.create(MarineWeatherAPIService::class.java)

        repository = BeachRepository(goMapsApi, BuildConfig.FSQ_API_KEY, marineWeatherApi)

        lifecycleScope.launch {
            try {
                showLoadingOverlay(true)
                val beachList = repository.getBeaches(latitude, longitude)
                beaches.clear()
                beaches.addAll(beachList)
                val db = Room.databaseBuilder(requireContext(), BeachLocalDB::class.java, "beach_db").build()

                beaches.forEach{ beach ->
                    val beachEntity = BeachEntity(beach.id, beach.name, beach.latitude, beach.longitude, beach.safetyStatus)
                    db.beachDAO().insertBeach(beachEntity)
                }
                beachViewModel.setBeaches(beachList)
                updateMap(beaches)
                Log.e(TAG,"${beaches.size} beaches found")
            } catch (e: Exception) {
                showLoadingOverlay(false)
                Log.e(TAG, "Failed to load beaches: ${e.message}")
                Toast.makeText(requireContext(), "Error loading beach data", Toast.LENGTH_SHORT).show()
            }finally {
                showLoadingOverlay(false)
            }
        }
    }

    private fun updateMap(beachList: List<Beach>) {
        googleMap.clear()
        beachList.forEach { beach ->
            val beachLoc = LatLng(beach.latitude, beach.longitude)
            val marker = MarkerOptions().position(beachLoc).title(beach.name)
            when(beach.safetyStatus.displayName){
                "Safe" -> marker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                "Caution" -> marker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))
                "Dangerous" -> marker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
            }


            googleMap.addMarker(marker)
        }
    }

    fun showLoadingOverlay(show:Boolean){
        binding.loadingOverlay.isVisible=show
    }
}
