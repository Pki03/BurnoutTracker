package com.example.burnouttracker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class MoodViewModel @Inject constructor(
    private val repository: MoodRepository
) : ViewModel() {
    val allMoods = repository.allEntries

    fun insertMood(mood: String, sleep: String) {
        viewModelScope.launch {
            repository.insert(MoodEntry(mood = mood, sleepHours = sleep))
        }
    }

    fun clearAllMoods() {
        viewModelScope.launch {
            repository.clearAll()
        }
    }
}
