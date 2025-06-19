package com.example.beachbuddy.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.beachbuddy.data.Beach

class BeachViewModel : ViewModel() {
    private val _beachList = MutableLiveData<List<Beach>>()
    val beachList : LiveData<List<Beach>> get() = _beachList

    private val _beach = MutableLiveData<Beach>()
    val beach : LiveData<Beach> get() = _beach

    fun setBeaches(beaches: List<Beach>) {
        _beachList.value = beaches
    }

    fun setBeach(beach: Beach){
        _beach.value = beach

    }

}