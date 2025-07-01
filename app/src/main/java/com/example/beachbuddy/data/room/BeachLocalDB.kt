package com.example.beachbuddy.data.room

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [BeachEntity::class], version = 1, exportSchema = false)
abstract class BeachLocalDB : RoomDatabase() {
    abstract fun beachDAO() : BeachDAO
    }