package com.example.burnouttracker.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.burnouttracker.ui.MoodViewModel
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoodScreen(viewModel: MoodViewModel = hiltViewModel()) {
    var mood by remember { mutableStateOf("") }
    var sleep by remember { mutableStateOf("") }
    var journal by remember { mutableStateOf("") }

    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val entries by viewModel.allMoods.collectAsState(initial = emptyList())

    val moodOptions = listOf("Happy", "Neutral", "Sad", "Stressed")
    var expanded by remember { mutableStateOf(false) }

    // Navigate when ViewModel flag is set
    LaunchedEffect(viewModel.shouldNavigateToHR.value) {
        if (viewModel.shouldNavigateToHR.value) {
            viewModel.resetNavigationFlag()
            val intent = Intent(context, HRDashboardActivity::class.java)
            context.startActivity(intent)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color(0xFFF0F4F8)
    ) { innerPadding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header
            item {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(
                        "BurnoutTracker",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = Color(0xFF00796B)
                    )
                }
            }

            // Mood input card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {

                        // Mood dropdown
                        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                            OutlinedTextField(
                                value = mood,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Mood") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                modifier = Modifier.menuAnchor().fillMaxWidth()
                            )
                            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                moodOptions.forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(option) },
                                        onClick = {
                                            mood = option
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(value = sleep, onValueChange = { sleep = it }, label = { Text("Sleep (hrs)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(value = journal, onValueChange = { journal = it }, label = { Text("Journal") }, maxLines = 4, modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Button(
                                onClick = {
                                    if (mood.isNotBlank() && sleep.isNotBlank() && journal.isNotBlank()) {
                                        viewModel.predictFitToWork(context, mood, sleep, journal) { predictionResult ->
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar("Prediction: $predictionResult")
                                            }

                                            // Insert mood entry
                                            viewModel.insertMood(mood, sleep, journal)

                                            // Clear input
                                            mood = ""
                                            sleep = ""
                                            journal = ""
                                        }
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00796B))
                            ) { Text("Submit", color = Color.White) }

                            Spacer(modifier = Modifier.width(12.dp))

                            OutlinedButton(
                                onClick = { viewModel.clearAllMoods() },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF00796B))
                            ) { Text("Clear All") }
                        }

                        if (mood == "Sad" || mood == "Stressed") {
                            Spacer(modifier = Modifier.height(20.dp))
                            Text("💡 Self-Care Tips", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = Color(0xFF37474F))
                            Spacer(modifier = Modifier.height(8.dp))

                            Text("🧘 Try a breathing exercise", color = Color(0xFF00796B), modifier = Modifier.clickable {
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=nmFUDkj1Aq0")))
                            })
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("🎧 Watch a meditation video", color = Color(0xFF00796B), modifier = Modifier.clickable {
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=inpok4MKVLM")))
                            })
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("💬 “This too shall pass.” – Persian Proverb", color = Color.DarkGray)
                        }
                    }
                }
            }

            // Mood history
            if (entries.isNotEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("Mood History", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = Color(0xFF37474F))
                    }
                }

                items(entries) { entry ->
                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Mood: ${entry.mood}")
                            Text("Sleep: ${entry.sleepHours} hrs")
                            Text("Sentiment: ${entry.sentiment}")
                            Text("Burnout Score: ${entry.burnoutScore}")
                            Text("Note: ${entry.journalEntry}")
                        }
                    }
                }

                item {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("Mood Over Time", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = Color(0xFF37474F))
                    }
                }

                item { MoodLineChart(entries = entries) }
            } else {
                item {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("No mood entries yet. Start tracking your mood!", modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                    }
                }
            }
        }
    }
}
