package com.example.burnouttracker.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.TopAppBarDefaults.centerAlignedTopAppBarColors
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Activity class for HR Dashboard
class HRDashboardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                HRDashboardScreen() // Compose function rendering the UI
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HRDashboardScreen() {
    val context = LocalContext.current // Get Android context for launching intents

    // Predefined list of HRs and counsellors with their emails
    val hrList = mapOf(
        "Priya Sharma (HR)" to "priya.hr@example.com",
        "Rahul Mehta (HR)" to "rahul.hr@example.com",
        "Dr. Ananya Jain (Counsellor)" to "ananya.counsellor@example.com",
        "Dr. Kabir Das (Counsellor)" to "kabir.counsellor@example.com"
    )

    // Dropdown state variables
    var expanded by remember { mutableStateOf(false) } // Controls dropdown visibility
    var selectedName by remember { mutableStateOf(hrList.keys.first()) } // Stores selected HR/Counsellor

    // Scaffold provides basic layout structure with TopAppBar and content
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "HR Dashboard",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                colors = centerAlignedTopAppBarColors(containerColor = Color(0xFFD32F2F)) // Red color for top bar
            )
        },
        containerColor = Color(0xFFFDECEA) // Background color of screen
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize() // Fill entire screen
                .padding(padding) // Apply scaffold padding
                .padding(16.dp), // Extra padding inside box
            contentAlignment = Alignment.TopCenter
        ) {
            // Card to hold the alert message and dropdown
            Card(
                shape = RoundedCornerShape(16.dp), // Rounded corners
                colors = CardDefaults.cardColors(containerColor = Color.White), // White background
                elevation = CardDefaults.cardElevation(8.dp), // Shadow for elevation
                modifier = Modifier
                    .fillMaxWidth() // Card takes full width
                    .padding(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp), // Space between items
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Alert Title
                    Text(
                        text = "Attention Required",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD32F2F) // Red color for importance
                        )
                    )

                    // Informative message
                    Text(
                        text = "You have been predicted as Unfit to Work 3 times in a row.",
                        fontSize = 18.sp,
                        color = Color.Black
                    )

                    Text(
                        text = "This may indicate serious stress, burnout, or health issues. Please notify a manager or counsellor below.",
                        fontSize = 16.sp,
                        color = Color.DarkGray
                    )

                    // Dropdown menu to select HR/Counsellor
                    ExposedDropdownMenuBox(
                        expanded = expanded, // Controls dropdown visibility
                        onExpandedChange = { expanded = !expanded } // Toggle dropdown
                    ) {
                        // Read-only text field showing selected name
                        OutlinedTextField(
                            readOnly = true,
                            value = selectedName,
                            onValueChange = {},
                            label = { Text("Select Person to Contact") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                            },
                            modifier = Modifier
                                .menuAnchor() // Attach dropdown menu to this field
                                .fillMaxWidth()
                        )

                        // Dropdown menu with HR/Counsellor names
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false } // Close menu when clicked outside
                        ) {
                            hrList.keys.forEach { name ->
                                DropdownMenuItem(
                                    text = { Text(name) },
                                    onClick = {
                                        selectedName = name // Update selected name
                                        expanded = false // Close dropdown
                                    }
                                )
                            }
                        }
                    }

                    // Email Button
                    Button(
                        onClick = {
                            val email = hrList[selectedName] ?: return@Button
                            // Encode subject and body for email intent
                            val subject = Uri.encode("Burnout Alert: Employee Unfit to Work")
                            val body = Uri.encode(
                                "Hi ${selectedName.split(" ")[0]},\n\n" +
                                        "An employee has been flagged unfit to work 3 times. Kindly reach out.\n\n" +
                                        "Regards,\nBurnout Tracker"
                            )
                            // Create mailto intent
                            val mailIntent = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("mailto:$email?subject=$subject&body=$body")
                            )
                            context.startActivity(mailIntent) // Launch email app
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Email ${selectedName.split(" ")[0]}", color = Color.White)
                    }

                    // Acknowledge Alert Button
                    OutlinedButton(
                        onClick = { /* logic for acknowledging alert */ },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Acknowledge Alert", color = Color(0xFFD32F2F))
                    }
                }
            }
        }
    }
}
