package com.example.beachbuddy.data.room

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.beachbuddy.data.SafetyStatus

@Entity(tableName = "beach_data")
data class BeachEntity(
    @PrimaryKey val id: String = "",
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val currentStatus : SafetyStatus
)

