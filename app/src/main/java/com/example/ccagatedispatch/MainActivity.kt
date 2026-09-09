package com.example.ccagatedispatch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ccagatedispatch.ui.coach.CoachScreen
import com.example.ccagatedispatch.ui.coach.CoachViewModel
import com.example.ccagatedispatch.ui.dispatch.DispatchScreen
import com.example.ccagatedispatch.ui.dispatch.DispatchViewModel
import com.example.ccagatedispatch.ui.entry.EntryScreen
import com.example.ccagatedispatch.ui.entry.EntryViewModel
import com.example.ccagatedispatch.ui.theme.CCAGateDispatchTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val entryViewModel: EntryViewModel by viewModels()
    private val coachViewModel: CoachViewModel by viewModels()
    private val dispatchViewModel: DispatchViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CCAGateDispatchTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(
                        entryViewModel = entryViewModel,
                        coachViewModel = coachViewModel,
                        dispatchViewModel = dispatchViewModel
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    entryViewModel: EntryViewModel,
    coachViewModel: CoachViewModel,
    dispatchViewModel: DispatchViewModel
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    val studentCount by entryViewModel.studentCount.collectAsState()
    val entryCount by entryViewModel.entryCount.collectAsState()
    val exitCount by entryViewModel.exitCount.collectAsState()
    val sessionCount by entryViewModel.sessionCount.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "CCA Gate Dispatch System",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "STRICT AIR-GAP | SQLCipher 256-Bit Encrypted DB",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.LightGray
                        )
                    }
                },
                actions = {
                    // System Dashboard Audit Counters
                    Row(
                        modifier = Modifier.padding(end = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AuditChip(label = "Students", count = studentCount)
                        AuditChip(label = "In Gate", count = entryCount)
                        AuditChip(label = "Exits", count = exitCount)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Login, contentDescription = null) },
                    label = { Text("Main Entrance") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.HowToReg, contentDescription = null) },
                    label = { Text("Coach Portal") }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.Security, contentDescription = null) },
                    label = { Text("Gate Dispatch") }
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> EntryScreen(viewModel = entryViewModel)
                1 -> CoachScreen(viewModel = coachViewModel)
                2 -> DispatchScreen(viewModel = dispatchViewModel)
            }
        }
    }
}

@Composable
fun AuditChip(label: String, count: Int) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$label: ",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = "$count",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
