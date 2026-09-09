package com.example.ccagatedispatch.ui.dispatch

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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

@Composable
fun DispatchScreen(
    viewModel: DispatchViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    var overrideReasonText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Commute Mode Tabs
        TabRow(selectedTabIndex = uiState.activeTab.ordinal) {
            Tab(
                selected = uiState.activeTab == DispatchTab.SELF_COMMUTE,
                onClick = { viewModel.handleEvent(DispatchEvent.SelectTab(DispatchTab.SELF_COMMUTE)) },
                text = { Text("Self Commute") },
                icon = { Icon(Icons.Default.Person, contentDescription = null) }
            )
            Tab(
                selected = uiState.activeTab == DispatchTab.PARENT_PICKUP,
                onClick = { viewModel.handleEvent(DispatchEvent.SelectTab(DispatchTab.PARENT_PICKUP)) },
                text = { Text("Parent Pickup") },
                icon = { Icon(Icons.Default.FamilyRestroom, contentDescription = null) }
            )
            Tab(
                selected = uiState.activeTab == DispatchTab.VAN_HANDOVER,
                onClick = { viewModel.handleEvent(DispatchEvent.SelectTab(DispatchTab.VAN_HANDOVER)) },
                text = { Text("Van Handover") },
                icon = { Icon(Icons.Default.DirectionsBus, contentDescription = null) }
            )
        }

        // Scanner / Search Input Field
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = uiState.searchInput,
                    onValueChange = { viewModel.handleEvent(DispatchEvent.UpdateSearchInput(it)) },
                    label = {
                        Text(
                            when (uiState.activeTab) {
                                DispatchTab.SELF_COMMUTE -> "Scan / Enter Student Card ID (e.g. STU_1001)"
                                DispatchTab.PARENT_PICKUP -> "Scan / Enter Parent Card ID (e.g. P_PAR_THORNE)"
                                DispatchTab.VAN_HANDOVER -> "Scan / Enter Driver ID (e.g. DRV_901)"
                            }
                        )
                    },
                    leadingIcon = { Icon(Icons.Default.QrCodeScanner, contentDescription = null) },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = { viewModel.handleEvent(DispatchEvent.ExecuteSearch(uiState.searchInput)) }
                ) {
                    Text("Search / Scan")
                }
            }
        }

        // Message / Alert
        uiState.message?.let { msg ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (uiState.isError) MaterialTheme.colorScheme.errorContainer else Color(0xFFE8F5E9)
                )
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (uiState.isError) Icons.Default.Warning else Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = if (uiState.isError) MaterialTheme.colorScheme.error else Color(0xFF2E7D32)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = msg, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        // Tab Specific Portal UI
        when (uiState.activeTab) {
            DispatchTab.SELF_COMMUTE -> SelfCommutePortal(uiState, viewModel)
            DispatchTab.PARENT_PICKUP -> ParentPickupPortal(uiState, viewModel)
            DispatchTab.VAN_HANDOVER -> VanHandoverPortal(uiState, viewModel)
        }
    }

    // Override Dialog for Self Commute
    if (uiState.showSelfOverrideDialog) {
        AlertDialog(
            onDismissRequest = { /* dismiss handled by buttons */ },
            title = { Text("Security Guard Override Authorization") },
            text = {
                Column {
                    Text("Student lacks Pre-Auth signature or class is still in progress. Enter justification override reason to release:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = overrideReasonText,
                        onValueChange = { overrideReasonText = it },
                        label = { Text("Override Reason") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.handleEvent(DispatchEvent.SubmitSelfOverride(overrideReasonText))
                        overrideReasonText = ""
                    },
                    enabled = overrideReasonText.isNotBlank()
                ) {
                    Text("Authorize Release")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.handleEvent(DispatchEvent.ClearMessage) }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SelfCommutePortal(uiState: DispatchState, viewModel: DispatchViewModel) {
    uiState.selfStudent?.let { student ->
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "SELF COMMUTE DISPATCH VERIFICATION", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Text(text = "Student Name: ${student.name}", style = MaterialTheme.typography.titleLarge)
                Text(text = "Class: ${student.std} - ${student.division}")
                Text(
                    text = "Pre-Auth Signed: ${if (student.selfAuth) "YES" else "NO (Requires Override)"}",
                    color = if (student.selfAuth) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { viewModel.handleEvent(DispatchEvent.ReleaseSelfStudent) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = if (student.selfAuth) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error)
                ) {
                    Text(if (student.selfAuth) "Authorize & Dispatch Student" else "Request Guard Override Release")
                }
            }
        }
    }
}

@Composable
private fun ParentPickupPortal(uiState: DispatchState, viewModel: DispatchViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Sibling Group Roster (${uiState.parentGroupStudents.size})",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(uiState.parentGroupStudents) { student ->
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
                            Text(text = "Parent: ${student.parentName} | Class: ${student.std}-${student.division}")
                        }
                        Button(
                            onClick = { viewModel.handleEvent(DispatchEvent.ReleaseParentStudent(student)) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                        ) {
                            Text("Release Student")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VanHandoverPortal(uiState: DispatchState, viewModel: DispatchViewModel) {
    uiState.activeVan?.let { van ->
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(text = "ACTIVE VAN ROUTE", fontWeight = FontWeight.Bold)
                    Text(text = "Route: ${van.route}", style = MaterialTheme.typography.titleMedium)
                    Text(text = "Driver: ${van.driverName} (ID: ${van.driverId})")
                }
            }

            Text(text = "Van Boarding Manifest", fontWeight = FontWeight.Bold)

            LazyColumn(
                modifier = Modifier.height(180.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(uiState.vanRoster) { student ->
                    val isChecked = uiState.vanCheckedStudents.contains(student.studentId)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isChecked,
                            onCheckedChange = { viewModel.handleEvent(DispatchEvent.ToggleVanStudentCheck(student.studentId)) }
                        )
                        Text(text = "${student.name} (${student.std} - ${student.division})", modifier = Modifier.weight(1f))
                    }
                }
            }

            SignatureCanvas(
                onSignatureCaptured = { svg ->
                    viewModel.handleEvent(DispatchEvent.SaveDigitalSignature(svg))
                },
                onClear = {
                    viewModel.handleEvent(DispatchEvent.SaveDigitalSignature(""))
                }
            )

            Button(
                onClick = { viewModel.handleEvent(DispatchEvent.CompleteVanHandover) },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState.vanCheckedStudents.isNotEmpty() && uiState.digitalSignatureSvg != null,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
            ) {
                Text("Dispatch Batch Van Release (${uiState.vanCheckedStudents.size} Boarded)")
            }
        }
    }
}
