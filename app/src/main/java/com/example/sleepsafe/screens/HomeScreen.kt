package com.example.sleepsafe.screens

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sleepsafe.viewmodel.HomeViewModel
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.getValue
import java.util.*

@Composable
fun HomeScreen(homeViewModel: HomeViewModel = viewModel()) {
    // Observing LiveData properties as Compose states
    val isRecording by homeViewModel.isRecording.observeAsState(false)
    val audioFilePath by homeViewModel.audioFilePath.observeAsState()
    val motionState by homeViewModel.motionState.observeAsState("No Movement")

    // Alarm observables
    var hour by remember { mutableStateOf(0) }
    var minute by remember { mutableStateOf(0) }
    var showTimePicker by remember { mutableStateOf(false) }
    var useSmartAlarm by remember { mutableStateOf(false) }
    val alarmTime by homeViewModel.alarmTime.observeAsState()

    // Enable Notification Dialog State
    var showNotificationSettings by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Set an Alarm", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))

        if (alarmTime != null) {
            val calendar = Calendar.getInstance().apply { timeInMillis = alarmTime!! }
            Text("Alarm set for: ${calendar.time}")
        } else {
            Text("No alarm set")
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { showNotificationSettings = true }) {
            Text("Enable Notifications")
        }

        if (showNotificationSettings) {
            AlertDialog(
                onDismissRequest = { showNotificationSettings = false },
                title = { Text("Enable Notifications") },
                text = { Text("Please enable notifications for SleepSafe to ensure alarms work correctly.") },
                confirmButton = {
                    TextButton(onClick = {
                        homeViewModel.requestExactAlarmPermission()
                        showNotificationSettings = false
                    }) {
                        Text("Go to Settings")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showNotificationSettings = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@SuppressLint("InlinedApi")
fun Context.openNotificationSettings() {
    val intent = Intent().apply {
        action = android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS
        putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, packageName)
        flags = Intent.FLAG_ACTIVITY_NEW_TASK // Add this flag
    }
    startActivity(intent)
}
