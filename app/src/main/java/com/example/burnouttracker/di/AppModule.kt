package com.example.burnouttracker.di

import android.app.Application
import androidx.room.Room
import com.example.burnouttracker.data.MoodDao
import com.example.burnouttracker.data.MoodDatabase
import com.example.burnouttracker.data.MoodRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(app: Application): MoodDatabase {
        return Room.databaseBuilder(
            app,
            MoodDatabase::class.java,
            "mood_db"
        ).build()
    }

    @Provides
    fun provideDao(db: MoodDatabase): MoodDao = db.moodDao()

    @Provides
    @Singleton
    fun provideRepository(dao: MoodDao): MoodRepository {
        return MoodRepository(dao)
    }
}