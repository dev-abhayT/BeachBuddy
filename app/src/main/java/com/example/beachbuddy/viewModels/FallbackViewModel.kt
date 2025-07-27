package com.example.beachbuddy.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.beachbuddy.data.FilterLocations

class FallbackViewModel : ViewModel(){
    private val _beachList = MutableLiveData<FilterLocations>()
    val beachList : LiveData<FilterLocations> get() = _beachList
    fun setLocation(location: FilterLocations) {
        _beachList.value = location
    }
}