package com.example.burnouttracker

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mood_table")
data class MoodEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val mood: String,
    val sleepHours: String,
    val timestamp: Long = System.currentTimeMillis()
)
