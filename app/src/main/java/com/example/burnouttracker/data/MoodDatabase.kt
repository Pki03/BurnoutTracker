package com.example.burnouttracker.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.burnouttracker.data.MoodEntry

@Database(entities = [MoodEntry::class], version = 1)
abstract class MoodDatabase : RoomDatabase(){

    abstract fun moodDao(): MoodDao

    companion object{
        @Volatile
        private var INSTANCE: MoodDatabase?=null

        fun getDatabase(context: Context): MoodDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MoodDatabase::class.java,
                    "mood_database"
                ).build()
                INSTANCE = instance
                instance
            }
    }
}
}