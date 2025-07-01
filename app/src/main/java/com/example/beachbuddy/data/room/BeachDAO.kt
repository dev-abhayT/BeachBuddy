package com.example.beachbuddy.data.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface BeachDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBeach (beach : BeachEntity)



    @Query("SELECT * FROM beach_data")
    suspend fun getAllBeaches() : List<BeachEntity>
}