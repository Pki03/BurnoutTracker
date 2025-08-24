package com.example.burnouttracker.ui

import android.graphics.Color
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.burnouttracker.data.MoodEntry
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.SimpleDateFormat
import java.util.*

// Composable function to display mood trends over time as a Line Chart
@Composable
fun MoodLineChart(entries: List<MoodEntry>) {
    val context = LocalContext.current // Get Android context for creating the chart view

    // AndroidView allows embedding traditional Android Views inside Jetpack Compose
    AndroidView(
        modifier = Modifier
            .fillMaxWidth() // Chart takes full width
            .height(300.dp) // Fixed height for chart
            .padding(top = 16.dp), // Top padding
        factory = {
            // Create and configure LineChart from MPAndroidChart library
            LineChart(context).apply {
                description.isEnabled = false // Remove default description text
                axisRight.isEnabled = false // Disable right Y-axis
                legend.isEnabled = true // Show legend
                animateXY(1000, 1000) // Animate X and Y axes over 1 second

                // Configure X-axis
                xAxis.position = XAxis.XAxisPosition.BOTTOM // Place labels at bottom
                xAxis.granularity = 1f // Interval between X-axis labels
                xAxis.setDrawGridLines(false) // Remove grid lines for clarity
            }
        },
        update = { chart ->
            // Map moods to numeric values for plotting
            val moodMap = mapOf("Happy" to 4f, "Neutral" to 3f, "Sad" to 2f, "Stressed" to 1f)

            // Sort mood entries by timestamp (older → left, newer → right)
            val sortedEntries = entries.sortedBy { it.timestamp }

            // Convert MoodEntry list into LineChart Entries
            val chartEntries = sortedEntries.mapIndexed { index, moodEntry ->
                Entry(index.toFloat(), moodMap[moodEntry.mood] ?: 0f) // Use mapped numeric value
            }

            // Create a LineDataSet (a series for the chart)
            val dataSet = LineDataSet(chartEntries, "Mood Level").apply {
                color = Color.BLUE // Line color
                valueTextColor = Color.BLACK // Value text color
                lineWidth = 2.5f // Line thickness
                circleRadius = 5f // Circle size for data points
                setCircleColor(Color.CYAN) // Circle color for points
                setDrawValues(false) // Don't draw values on top of points
                mode = LineDataSet.Mode.CUBIC_BEZIER // Smooth curve instead of straight lines
            }

            chart.data = LineData(dataSet) // Set data for the chart

            // Format X-axis labels to show timestamp
            chart.xAxis.valueFormatter = object : ValueFormatter() {
                private val sdf = SimpleDateFormat("dd MMM\nHH:mm", Locale.getDefault()) // Date format
                override fun getFormattedValue(value: Float): String {
                    // Ensure index is within bounds
                    val index = value.toInt().coerceIn(sortedEntries.indices)
                    return sdf.format(Date(sortedEntries[index].timestamp)) // Format timestamp for label
                }
            }

            chart.invalidate() // Refresh chart to display updated data
        }
    )
}
