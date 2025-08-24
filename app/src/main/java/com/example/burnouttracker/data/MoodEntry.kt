package com.example.burnouttracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mood_table")
data class MoodEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val mood: String,
    val sleepHours: String,
    val journalEntry: String,
    val sentiment: String = "",        // Default: to be auto-generated
    val burnoutScore: Int = 0,         // Default: to be calculated
    val timestamp: Long = System.currentTimeMillis()
)