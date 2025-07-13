package com.example.burnouttracker

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State

@HiltViewModel
class MoodViewModel @Inject constructor(
    private val repository: MoodRepository
) : ViewModel() {

    private val _unfitCount = mutableStateOf(0)
    val unfitCount: State<Int> get() = _unfitCount

    private val _shouldNavigateToHR = mutableStateOf(false)
    val shouldNavigateToHR: State<Boolean> get() = _shouldNavigateToHR

    fun incrementUnfitCount() {
        _unfitCount.value++
    }

    fun resetUnfitCount() {
        _unfitCount.value = 0
    }

    fun resetNavigationFlag() {
        _shouldNavigateToHR.value = false
    }

    val allMoods = repository.allEntries

    fun insertMood(mood: String, sleep: String, journalEntry: String) {
        val sleepHours = sleep.toFloatOrNull() ?: return
        val sentiment = getSentiment(journalEntry)
        val burnoutScore = calculateBurnoutScore(mood, sleepHours, sentiment)

        val entry = MoodEntry(
            mood = mood,
            sleepHours = sleep,
            journalEntry = journalEntry,
            sentiment = sentiment,
            burnoutScore = burnoutScore
        )

        viewModelScope.launch {
            repository.insert(entry)
        }
    }

    fun clearAllMoods() {
        viewModelScope.launch {
            repository.clearAll()
        }
    }

    private fun getSentiment(text: String): String {
        val lower = text.lowercase()
        val positives = listOf("happy", "relaxed", "excited", "joy", "good", "calm")
        val negatives = listOf("sad", "tired", "angry", "stressed", "anxious", "bad")

        val score = positives.count { it in lower } - negatives.count { it in lower }

        return when {
            score > 0 -> "Positive"
            score < 0 -> "Negative"
            else -> "Neutral"
        }
    }

    private fun calculateBurnoutScore(mood: String, sleep: Float, sentiment: String): Int {
        var score = 100

        if (sleep < 6f) score -= 15
        else if (sleep > 9f) score -= 10

        if (mood.lowercase() in listOf("sad", "tired", "angry", "low")) score -= 20

        score -= when (sentiment) {
            "Negative" -> 15
            "Neutral" -> 5
            else -> 0
        }

        return score.coerceIn(0, 100)
    }

    fun predictFitToWork(
        context: Context,
        mood: String,
        sleep: String,
        journal: String,
        onResult: (String) -> Unit
    ) {
        try {
            val interpreter = Interpreter(loadModelFile(context))

            val moodValue = when (mood) {
                "Happy" -> 0f
                "Neutral" -> 1f
                "Tired" -> 2f
                "Stressed" -> 3f
                else -> 4f // fallback for unknown
            }

            val sleepHours = sleep.toFloatOrNull() ?: 0f
            val sentimentScore = getSentimentScore(journal)

            val scaledInput = scaleInput(moodValue, sleepHours, sentimentScore)
            val input = arrayOf(scaledInput)

            val output = Array(1) { FloatArray(1) }

            interpreter.run(input, output)

            val rawOutput = output[0][0]

            val prediction = if (rawOutput < 0.5f) "Fit" else "Unfit"

            Log.d("ML_INPUT", "Input: mood=$moodValue sleep=$sleepHours sentiment=$sentimentScore")
            Log.d("ML_OUTPUT", "Raw Output = $rawOutput -> Prediction = $prediction")

            if (prediction == "Unfit") {
                _unfitCount.value++
                if (_unfitCount.value >= 3) {
                    _shouldNavigateToHR.value = true
                }
            } else {
                resetUnfitCount()
            }

            onResult(prediction)
        } catch (e: Exception) {
            e.printStackTrace()
            onResult("Error: ${e.message ?: "Model failed to load or run"}")
        }
    }

    fun scaleInput(mood: Float, sleep: Float, sentiment: Float): FloatArray {
        val moodMin = 1f
        val moodMax = 9f

        val sleepMin = 3f
        val sleepMax = 8f

        val sentMin = -0.9f
        val sentMax = 0.9f

        val scaledMood = (mood - moodMin) / (moodMax - moodMin)
        val scaledSleep = (sleep - sleepMin) / (sleepMax - sleepMin)
        val scaledSentiment = (sentiment - sentMin) / (sentMax - sentMin)

        return floatArrayOf(scaledMood, scaledSleep, scaledSentiment)
    }

    private fun loadModelFile(context: Context): MappedByteBuffer {
        val fileDescriptor = context.assets.openFd("fit_predictor.tflite")
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    fun getSentimentScore(journal: String): Float {
        val lower = journal.lowercase()
        return when {
            "sad" in lower || "angry" in lower || "tired" in lower -> -0.8f
            "happy" in lower || "great" in lower || "productive" in lower -> 0.9f
            else -> 0.1f
        }
    }
}
