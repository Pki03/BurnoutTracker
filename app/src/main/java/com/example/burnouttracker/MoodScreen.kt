package com.example.burnouttracker

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
    val unfitCount by viewModel.unfitCount

    val moodOptions = listOf("Happy", "Neutral", "Sad", "Stressed")
    var expanded by remember { mutableStateOf(false) }

    // 🔁 Navigate to HR screen when unfit count reaches 3
    LaunchedEffect(unfitCount) {
        if (unfitCount >= 3) {
            viewModel.resetUnfitCount()
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
            item {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "BurnoutTracker",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = Color(0xFF00796B)
                    )
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {

                        // Mood Dropdown
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded }
                        ) {
                            OutlinedTextField(
                                value = mood,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Mood") },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                                },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                            )

                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
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

                        OutlinedTextField(
                            value = sleep,
                            onValueChange = { sleep = it },
                            label = { Text("Sleep (hrs)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = journal,
                            onValueChange = { journal = it },
                            label = { Text("Journal") },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 4
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Button(
                                onClick = {
                                    if (mood.isNotBlank() && sleep.isNotBlank() && journal.isNotBlank()) {
                                        viewModel.predictFitToWork(
                                            context = context,
                                            mood = mood,
                                            sleep = sleep,
                                            journal = journal
                                        ) { predictionResult ->
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar("Prediction: $predictionResult")
                                            }

                                            if (predictionResult == "Unfit") {
                                                viewModel.incrementUnfitCount()
                                            }

                                            viewModel.insertMood(mood, sleep, journal)

                                            // Clear inputs
                                            mood = ""
                                            sleep = ""
                                            journal = ""
                                        }
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00796B))
                            ) {
                                Text("Submit", color = Color.White)
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            OutlinedButton(
                                onClick = { viewModel.clearAllMoods() },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF00796B))
                            ) {
                                Text("Clear All")
                            }
                        }

                        // Tips if mood is sad or stressed
                        if (mood == "Sad" || mood == "Stressed") {
                            Spacer(modifier = Modifier.height(20.dp))
                            Text(
                                text = "💡 Self-Care Tips",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFF37474F)
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "🧘 Try a breathing exercise",
                                color = Color(0xFF00796B),
                                modifier = Modifier.clickable {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=nmFUDkj1Aq0"))
                                    context.startActivity(intent)
                                }
                            )
                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "🎧 Watch a meditation video",
                                color = Color(0xFF00796B),
                                modifier = Modifier.clickable {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=inpok4MKVLM"))
                                    context.startActivity(intent)
                                }
                            )
                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "💬 “This too shall pass.” – Persian Proverb",
                                color = Color.DarkGray
                            )
                        }
                    }
                }
            }

            if (entries.isNotEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Mood History",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF37474F)
                        )
                    }
                }

                items(entries) { entry ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
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
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Mood Over Time",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF37474F)
                        )
                    }
                }

                item {
                    MoodLineChart(entries = entries)
                }
            } else {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No mood entries yet. Start tracking your mood!",
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}
