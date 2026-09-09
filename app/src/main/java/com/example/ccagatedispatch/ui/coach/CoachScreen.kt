package com.example.ccagatedispatch.ui.coach

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoachScreen(
    viewModel: CoachViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    var coachDropdownExpanded by remember { mutableStateOf(false) }
    var ccaDropdownExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Coach & CCA Selection Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Coach Check-In & Session Control",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Coach Dropdown
                    ExposedDropdownMenuBox(
                        expanded = coachDropdownExpanded,
                        onExpandedChange = { coachDropdownExpanded = !coachDropdownExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = uiState.selectedCoach?.name ?: "Select Coach",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Coach") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = coachDropdownExpanded) },
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = coachDropdownExpanded,
                            onDismissRequest = { coachDropdownExpanded = false }
                        ) {
                            uiState.coaches.forEach { coach ->
                                DropdownMenuItem(
                                    text = { Text(coach.name) },
                                    onClick = {
                                        viewModel.handleEvent(CoachEvent.SelectCoach(coach))
                                        coachDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // CCA Dropdown
                    ExposedDropdownMenuBox(
                        expanded = ccaDropdownExpanded,
                        onExpandedChange = { ccaDropdownExpanded = !ccaDropdownExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = uiState.selectedCca?.name ?: "Select CCA",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("CCA Class") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = ccaDropdownExpanded) },
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = ccaDropdownExpanded,
                            onDismissRequest = { ccaDropdownExpanded = false }
                        ) {
                            uiState.ccas.forEach { cca ->
                                DropdownMenuItem(
                                    text = { Text("${cca.name} (${cca.schedule})") },
                                    onClick = {
                                        viewModel.handleEvent(CoachEvent.SelectCca(cca))
                                        ccaDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { viewModel.handleEvent(CoachEvent.CheckInCoach) },
                        enabled = !uiState.isCoachCheckedIn
                    ) {
                        Text(if (uiState.isCoachCheckedIn) "Coach Checked In" else "Confirm Coach Check-In")
                    }

                    Button(
                        onClick = { viewModel.handleEvent(CoachEvent.CompleteClassSession) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                        enabled = uiState.isCoachCheckedIn && !uiState.isClassCompleted
                    ) {
                        Text(if (uiState.isClassCompleted) "Session Finalized" else "Complete Class & Open Exit Window")
                    }
                }
            }
        }

        // Feedback / Alert Message
        uiState.message?.let { msg ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF1565C0))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = msg, style = MaterialTheme.typography.bodySmall, color = Color(0xFF0D47A1))
                }
            }
        }

        // Student Roster Attendance Toggles
        Text(
            text = "Enrolled Student Roster (${uiState.enrolledStudents.size})",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(uiState.enrolledStudents) { student ->
                val currentStatus = uiState.studentAttendanceMap[student.studentId] ?: "Not Marked"

                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = student.name, fontWeight = FontWeight.Bold)
                            Text(
                                text = "ID: ${student.studentId} | Class: ${student.std}-${student.division}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Button(
                                onClick = {
                                    viewModel.handleEvent(CoachEvent.ToggleStudentAttendance(student.studentId, "Present"))
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (currentStatus == "Present") Color(0xFF2E7D32) else Color.LightGray
                                )
                            ) {
                                Text("Present")
                            }

                            OutlinedButton(
                                onClick = {
                                    viewModel.handleEvent(CoachEvent.ToggleStudentAttendance(student.studentId, "Absent"))
                                }
                            ) {
                                Text("Absent")
                            }
                        }
                    }
                }
            }
        }
    }
}
