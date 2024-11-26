package com.example.sleepsafe.screens

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
import androidx.compose.ui.platform.LocalContext
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(homeViewModel: HomeViewModel = viewModel()) {

    val context = LocalContext.current

    // Observing LiveData properties as Compose states
    val sleepTime by homeViewModel.sleepTime.observeAsState()
    val isTracking by homeViewModel.isTracking.observeAsState(false)
    val alarmTime by homeViewModel.alarmTime.observeAsState()
    val permissionRequired by homeViewModel.permissionRequired.observeAsState(false)

    // Dialog states
    var showPermissionDialog by remember { mutableStateOf(false) }
    var showAlarmTimePicker by remember { mutableStateOf(false) }
    var showSleepTimePicker by remember { mutableStateOf(false) }

    // Variables to store selected time
    var alarmHour by remember { mutableStateOf(7) } // Default alarm hour
    var alarmMinute by remember { mutableStateOf(30) } // Default alarm minute

    // Format sleep time and alarm time for display
    val sleepTimeFormatted = sleepTime?.let {
        SimpleDateFormat("hh:mm a", Locale.getDefault()).format(it.time)
    } ?: "Not Set"

    val alarmTimeFormatted = alarmTime?.let {
        val calendar = Calendar.getInstance().apply { timeInMillis = it }
        SimpleDateFormat("hh:mm a", Locale.getDefault()).format(calendar.time)
    } ?: "Not Set"

    // UI Layout
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Alarm Section
        AlarmSection(
            alarmTimeFormatted = alarmTimeFormatted,
            onSetAlarmClick = {
                if (permissionRequired) {
                    showPermissionDialog = true
                } else {
                    showAlarmTimePicker = true
                }
            },
            onCancelAlarmClick = {
                homeViewModel.cancelAlarm()
                Toast.makeText(context, "Alarm Canceled", Toast.LENGTH_SHORT).show()
            }
        )

        // Sleep Time Section
        SleepTimeSection(
            sleepTimeFormatted = sleepTimeFormatted,
            onSetSleepTimeClick = { showSleepTimePicker = true },
            onClearSleepTimeClick = {
                if (sleepTime != null) {
                    homeViewModel.clearSleepTime()
                    Toast.makeText(context, "Sleep Time Cleared", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "No Sleep Time Set", Toast.LENGTH_SHORT).show()
                }
            }
        )

        // Tracking Section
        TrackingSection(
            isTracking = isTracking,
            onStartTrackingClick = {
                homeViewModel.startTrackingNow(context)
                Toast.makeText(context, "Sleep Tracking Started", Toast.LENGTH_SHORT).show()
            },
            onStopTrackingClick = {
                homeViewModel.stopTracking(context)
                Toast.makeText(context, "Sleep Tracking Stopped", Toast.LENGTH_SHORT).show()
            }
        )
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

    // Alarm Time Picker Dialog
    if (showAlarmTimePicker) {
        TimePickerDialog(
            initialHour = alarmHour,
            initialMinute = alarmMinute,
            onTimeSelected = { selectedHour, selectedMinute ->
                alarmHour = selectedHour
                alarmMinute = selectedMinute
                homeViewModel.setAlarm(alarmHour, alarmMinute, useSmartAlarm = false)
                Toast.makeText(
                    context,
                    "Alarm set for $alarmHour:$alarmMinute",
                    Toast.LENGTH_SHORT
                ).show()
                showAlarmTimePicker = false
            },
            onDismiss = { showAlarmTimePicker = false }
        )
    }

    // Sleep Time Picker Dialog
    if (showSleepTimePicker) {
        TimePickerDialog(
            initialHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
            initialMinute = Calendar.getInstance().get(Calendar.MINUTE),
            onTimeSelected = { selectedHour, selectedMinute ->
                homeViewModel.setSleepTime(selectedHour, selectedMinute)
                Toast.makeText(
                    context,
                    "Sleep Time set for $selectedHour:$selectedMinute",
                    Toast.LENGTH_SHORT
                ).show()
                showSleepTimePicker = false
            },
            onDismiss = { showSleepTimePicker = false }
        )
    }
}

@Composable
fun AlarmSection(
    alarmTimeFormatted: String,
    onSetAlarmClick: () -> Unit,
    onCancelAlarmClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = "Alarm Time: $alarmTimeFormatted", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onSetAlarmClick) {
            Text(text = "Set Alarm")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onCancelAlarmClick) {
            Text(text = "Cancel Alarm")
        }
    }
}

@Composable
fun SleepTimeSection(
    sleepTimeFormatted: String,
    onSetSleepTimeClick: () -> Unit,
    onClearSleepTimeClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = "Sleep Time: $sleepTimeFormatted", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onSetSleepTimeClick) {
            Text(text = "Set Sleep Time")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onClearSleepTimeClick) {
            Text(text = "Clear Sleep Time")
        }
    }
}

@Composable
fun TrackingSection(
    isTracking: Boolean,
    onStartTrackingClick: () -> Unit,
    onStopTrackingClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        if (!isTracking) {
            Button(onClick = onStartTrackingClick) {
                Text(text = "Start Tracking Now")
            }
        } else {
            Button(onClick = onStopTrackingClick) {
                Text(text = "Stop Tracking")
            }
        }
    }
}
