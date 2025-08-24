package com.example.burnouttracker.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.burnouttracker.data.MoodEntry
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