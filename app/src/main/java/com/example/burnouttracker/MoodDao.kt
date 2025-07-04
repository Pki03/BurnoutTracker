package com.example.burnouttracker

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MoodDao {

    @Insert
    suspend fun insertMood(entry: MoodEntry)

    @Query("SELECT * FROM mood_table ORDER BY timestamp DESC")
    fun getAllMoods(): Flow<List<MoodEntry>>

    @Query("DELETE FROM mood_table")
    suspend fun clearAll()
}