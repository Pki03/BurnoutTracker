package com.example.burnouttracker

import android.graphics.Color
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet

@Composable
fun MoodLineChart(entries: List<MoodEntry>) {
    val context = LocalContext.current

    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .padding(top = 16.dp),
        factory = {
            LineChart(context).apply {
                description.isEnabled = false
                xAxis.position = XAxis.XAxisPosition.BOTTOM
                axisRight.isEnabled = false
                legend.isEnabled = true
                animateXY(1000, 1000)
            }
        },
        update = { chart ->
            val moodMap = mapOf("Happy" to 5f, "Neutral" to 3f, "Sad" to 1f, "Stressed" to 0f)
            val chartEntries = entries.mapIndexed { index, moodEntry ->
                Entry(index.toFloat(), moodMap[moodEntry.mood] ?: 2f)
            }

            val dataSet = LineDataSet(chartEntries, "Mood Level").apply {
                color = Color.BLUE
                valueTextColor = Color.BLACK
                lineWidth = 2f
                circleRadius = 3f
                setCircleColor(Color.RED)
            }

            chart.data = LineData(dataSet)
            chart.invalidate()
        }
    )
}
