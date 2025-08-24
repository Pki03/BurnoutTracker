package com.example.burnouttracker.data

import kotlinx.coroutines.flow.Flow

class MoodRepository(private val dao: MoodDao) {

    val allEntries: Flow<List<MoodEntry>> = dao.getAllMoods()

    suspend fun insert(entry: MoodEntry) {
        dao.insertMood(entry)
    }

    suspend fun clearAll() {
        dao.clearAll()
    }
}