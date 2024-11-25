package com.example.sleepsafe.screens

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.os.Build
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
import java.util.*

@Composable
fun HomeScreen(homeViewModel: HomeViewModel = viewModel()) {
    // Observing LiveData properties as Compose states
    val alarmTime by homeViewModel.alarmTime.observeAsState()
    val permissionRequired by homeViewModel.permissionRequired.observeAsState(false)

    // Dialog state
    var showPermissionDialog by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    // Variables to store selected time
    var hour by remember { mutableStateOf(7) } // Default hour
    var minute by remember { mutableStateOf(30) } // Default minute

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Set an Alarm", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))

        // Display Alarm Time or No Alarm Text
        if (alarmTime != null) {
            val calendar = Calendar.getInstance().apply { timeInMillis = alarmTime!! }
            Text(text = "Alarm set for: ${calendar.time}")
        } else {
            Text(text = "No alarm set")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Set Alarm Button
        Button(onClick = {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && permissionRequired) {
                showPermissionDialog = true
            } else {
                showPermissionDialog = false
                showTimePicker = true // Show the time picker dialog
            }
        }) {
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

        // Permission Dialog
        if (showPermissionDialog) {
            AlertDialog(
                onDismissRequest = { showPermissionDialog = false },
                title = { Text("Permission Required") },
                text = { Text("This app needs permission to schedule exact alarms.") },
                confirmButton = {
                    TextButton(onClick = {
                        homeViewModel.requestExactAlarmPermission()
                        showPermissionDialog = false
                    }) {
                        Text("Grant Permission")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showPermissionDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Time Picker Dialog
        if (showTimePicker) {
            TimePickerDialog(
                initialHour = hour,
                initialMinute = minute,
                onTimeSelected = { selectedHour, selectedMinute ->
                    hour = selectedHour
                    minute = selectedMinute
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
    }
}
