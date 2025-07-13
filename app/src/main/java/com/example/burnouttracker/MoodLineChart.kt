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
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.SimpleDateFormat
import java.util.*

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
                axisRight.isEnabled = false
                legend.isEnabled = true
                animateXY(1000, 1000)

                xAxis.position = XAxis.XAxisPosition.BOTTOM
                xAxis.granularity = 1f
                xAxis.setDrawGridLines(false)
            }
        },
        update = { chart ->
            val moodMap = mapOf("Happy" to 4f, "Neutral" to 3f, "Sad" to 2f, "Stressed" to 1f)

            // Sort entries by timestamp so left = oldest, right = newest
            val sortedEntries = entries.sortedBy { it.timestamp }

            val chartEntries = sortedEntries.mapIndexed { index, moodEntry ->
                Entry(index.toFloat(), moodMap[moodEntry.mood] ?: 0f)
            }

            val dataSet = LineDataSet(chartEntries, "Mood Level").apply {
                color = Color.BLUE
                valueTextColor = Color.BLACK
                lineWidth = 2.5f
                circleRadius = 5f
                setCircleColor(Color.CYAN)
                setDrawValues(false)
                mode = LineDataSet.Mode.CUBIC_BEZIER
            }

            chart.data = LineData(dataSet)

            // Update the X-axis labels after sorting
            chart.xAxis.valueFormatter = object : ValueFormatter() {
                private val sdf = SimpleDateFormat("dd MMM\nHH:mm", Locale.getDefault())
                override fun getFormattedValue(value: Float): String {
                    val index = value.toInt().coerceIn(sortedEntries.indices)
                    return sdf.format(Date(sortedEntries[index].timestamp))
                }
            }

            chart.invalidate()
        }
    )
}
