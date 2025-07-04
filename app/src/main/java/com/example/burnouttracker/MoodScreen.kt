package com.example.burnouttracker

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun MoodScreen(viewModel: MoodViewModel = hiltViewModel()) {
    var mood by remember { mutableStateOf("") }
    var sleep by remember { mutableStateOf("") }

    val entries by viewModel.allMoods.collectAsState(initial = emptyList())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Text(
            text = "Mood & Sleep Tracker",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Input Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = mood,
                    onValueChange = { mood = it },
                    label = { Text("Enter Mood") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = sleep,
                    onValueChange = { sleep = it },
                    label = { Text("Sleep Hours") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = {
                            if (mood.isNotBlank() && sleep.isNotBlank()) {
                                viewModel.insertMood(mood, sleep)
                                mood = ""
                                sleep = ""
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Submit")
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    OutlinedButton(
                        onClick = { viewModel.clearAllMoods() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Clear All")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // History Section
        Text("Mood History", style = MaterialTheme.typography.titleMedium)

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            if (entries.isEmpty()) {
                Text(
                    text = "No data available.",
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.CenterHorizontally)
                )
            } else {
                LazyColumn(modifier = Modifier.padding(16.dp)) {
                    items(entries) { entry ->
                        Text("${entry.mood} — ${entry.sleepHours} hrs", modifier = Modifier.padding(vertical = 4.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Chart
        if (entries.isNotEmpty()) {
            Text("Mood Over Time", style = MaterialTheme.typography.titleMedium)
            MoodLineChart(entries = entries)
        }
    }
}
