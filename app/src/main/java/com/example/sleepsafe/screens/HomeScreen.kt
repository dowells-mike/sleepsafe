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
        Text(text = "Set an Alarm", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))

        // Show Alarm Info
        if (alarmTime != null) {
            val calendar = Calendar.getInstance().apply { timeInMillis = alarmTime!! }
            Text(text = "Alarm set for: ${calendar.time}")
        } else {
            Text(text = "No alarm set")
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Set Alarm Button
        Button(onClick = { showTimePicker = true }) {
            Text(text = "Set Alarm")
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Cancel Alarm Button
        Button(onClick = {
            homeViewModel.cancelAlarm()
            Toast.makeText(homeViewModel.getApplication(), "Alarm Canceled", Toast.LENGTH_SHORT).show()
        }) {
            Text(text = "Cancel Alarm")
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Enable Notifications Button
        Button(onClick = { showNotificationSettings = true }) {
            Text(text = "Enable Notifications")
        }

        // Time Picker Dialog
        if (showTimePicker) {
            TimePickerDialog(
                initialHour = 7,
                initialMinute = 30,
                onTimeSelected = { hour, minute ->
                    homeViewModel.setAlarm(hour, minute, useSmartAlarm = false)
                    Toast.makeText(
                        homeViewModel.getApplication(),
                        "Alarm set for $hour:$minute",
                        Toast.LENGTH_SHORT
                    ).show()
                    showTimePicker = false
                },
                onDismiss = { showTimePicker = false }
            )
        }

        // Notification Settings Dialog
        if (showNotificationSettings) {
            AlertDialog(
                onDismissRequest = { showNotificationSettings = false },
                title = { Text("Enable Notifications") },
                text = {
                    Text("Notifications are required for alarms to work correctly. Please enable notifications for SleepSafe in the app settings.")
                },
                confirmButton = {
                    TextButton(onClick = {
                        showNotificationSettings = false
                        homeViewModel.getApplication<Application>().openNotificationSettings()
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
