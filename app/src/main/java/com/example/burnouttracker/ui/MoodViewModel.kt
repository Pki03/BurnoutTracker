package com.example.burnouttracker.ui

import android.content.Context
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.burnouttracker.data.MoodEntry
import com.example.burnouttracker.data.MoodRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import javax.inject.Inject

@HiltViewModel
class MoodViewModel @Inject constructor(
    private val repository: MoodRepository
) : ViewModel() {

    private val _unfitCount = mutableStateOf(0)
    val unfitCount: State<Int> get() = _unfitCount

    private val _shouldNavigateToHR = mutableStateOf(false)
    val shouldNavigateToHR: State<Boolean> get() = _shouldNavigateToHR

    fun incrementUnfitCount() { _unfitCount.value++ }
    fun resetUnfitCount() { _unfitCount.value = 0 }
    fun resetNavigationFlag() { _shouldNavigateToHR.value = false }

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
        viewModelScope.launch { repository.insert(entry) }
    }

    fun clearAllMoods() {
        viewModelScope.launch { repository.clearAll() }
    }

    private fun getSentiment(text: String): String {
        val lower = text.lowercase()
        val positives = listOf("happy", "relaxed", "excited", "joy", "good", "calm","energetic","positive")
        val negatives = listOf("sad", "tired", "angry", "stressed", "anxious", "bad","drowsy","sleepy","exhausted")
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

    // ==================== Corrected ML Prediction ====================
    fun predictFitToWork(
        context: Context,
        mood: String,
        sleep: String,
        journal: String,
        onResult: (String) -> Unit
    ) {
        try {
            val interpreter = Interpreter(loadModelFile(context))

            // Map mood to 1–9 scale matching Python training
            val moodValue = when (mood) {
                "Happy" -> 9f
                "Neutral" -> 5f
                "Tired" -> 3f
                "Sad" -> 1f
                "Stressed" -> 2f
                else -> 4f
            }

            // Convert sleep hours
            val sleepHours = sleep.toFloatOrNull() ?: 0f

            // Sentiment score: -1,0,1
            val sentimentScore = getSentimentScore(journal)

            // Scale inputs to 0–1 using Python training min/max
            val input = scaleInput(moodValue, sleepHours, sentimentScore)

            // TFLite input/output
            val tfliteInput = arrayOf(input)
            val tfliteOutput = Array(1) { FloatArray(1) }

            interpreter.run(tfliteInput, tfliteOutput)

            val rawOutput = tfliteOutput[0][0]

            val prediction = if (rawOutput < 0.5f) "Fit" else "Unfit"

            Log.d("ML_INPUT", "Mood=$moodValue Sleep=$sleepHours Sentiment=$sentimentScore")
            Log.d("ML_OUTPUT", "Raw=$rawOutput Predicted=$prediction")

            // Update unfit counter and navigation
            if (prediction == "Unfit") {
                _unfitCount.value++
                if (_unfitCount.value >= 3) _shouldNavigateToHR.value = true
            } else {
                resetUnfitCount()
            }

            onResult(prediction)

        } catch (e: Exception) {
            e.printStackTrace()
            onResult("Error: ${e.message ?: "Model failed"}")
        }
    }

    private fun scaleInput(mood: Float, sleep: Float, sentiment: Float): FloatArray {
        val moodMin = 1f
        val moodMax = 9f
        val sleepMin = 0f
        val sleepMax = 10f
        val sentMin = -1f
        val sentMax = 1f

        val scaledMood = (mood - moodMin) / (moodMax - moodMin)
        val scaledSleep = (sleep - sleepMin) / (sleepMax - sleepMin)
        val scaledSentiment = (sentiment - sentMin) / (sentMax - sentMin)

        return floatArrayOf(scaledMood, scaledSleep, scaledSentiment)
    }

    private fun loadModelFile(context: Context): MappedByteBuffer {
        val fileDescriptor = context.assets.openFd("fit_predictor.tflite")
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val channel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return channel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    private fun getSentimentScore(journal: String): Float {
        val lower = journal.lowercase()
        return when {
            "sad" in lower || "angry" in lower || "tired" in lower || "stressed" in lower || "exhausted" in lower -> -1.0f
            "happy" in lower || "great" in lower || "productive" in lower -> 1.0f
            else -> 0.0f
        }
    }
}
