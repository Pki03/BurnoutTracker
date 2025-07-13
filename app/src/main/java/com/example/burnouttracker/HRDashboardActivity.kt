package com.example.burnouttracker

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

class HRDashboardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                HRDashboardScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HRDashboardScreen() {
    val context = LocalContext.current

    // HR/Counsellor list
    val hrList = mapOf(
        "Priya Sharma (HR)" to "priya.hr@example.com",
        "Rahul Mehta (HR)" to "rahul.hr@example.com",
        "Dr. Ananya Jain (Counsellor)" to "ananya.counsellor@example.com",
        "Dr. Kabir Das (Counsellor)" to "kabir.counsellor@example.com"
    )

    var expanded by remember { mutableStateOf(false) }
    var selectedName by remember { mutableStateOf(hrList.keys.first()) }

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
                colors = centerAlignedTopAppBarColors(containerColor = Color(0xFFD32F2F))
            )
        },
        containerColor = Color(0xFFFDECEA)
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Attention Required",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD32F2F)
                        )
                    )

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

                    // Dropdown to select HR/Counsellor
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            readOnly = true,
                            value = selectedName,
                            onValueChange = {},
                            label = { Text("Select Person to Contact") },
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
                            hrList.keys.forEach { name ->
                                DropdownMenuItem(
                                    text = { Text(name) },
                                    onClick = {
                                        selectedName = name
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Email Button
                    Button(
                        onClick = {
                            val email = hrList[selectedName] ?: return@Button
                            val subject = Uri.encode("Burnout Alert: Employee Unfit to Work")
                            val body = Uri.encode("Hi ${selectedName.split(" ")[0]},\n\nAn employee has been flagged unfit to work 3 times. Kindly reach out.\n\nRegards,\nBurnout Tracker")
                            val mailIntent = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("mailto:$email?subject=$subject&body=$body")
                            )
                            context.startActivity(mailIntent)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Email ${selectedName.split(" ")[0]}", color = Color.White)
                    }

                    // Acknowledge Alert
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
