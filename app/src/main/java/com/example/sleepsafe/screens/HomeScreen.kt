// HomeScreen.kt
package com.example.sleepsafe.screens

import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sleepsafe.viewmodel.HomeViewModel
import java.util.Calendar

/**
 * Displays the Home screen with functionalities for managing sleep tracking, alarms, and sleep time.
 *
 * @param homeViewModel The ViewModel to handle the logic and state of the Home screen.
 */
@RequiresApi(Build.VERSION_CODES.S)
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

    // Time formatting
    val sleepTimeFormatted = sleepTime?.let { homeViewModel.formatTime(it) } ?: "Not Set"
    val alarmTimeFormatted = alarmTime?.let {
        val calendar = Calendar.getInstance().apply { timeInMillis = it }
        homeViewModel.formatTime(calendar)
    } ?: "Not Set"

    // Layout
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
            onCancelAlarmClick = { homeViewModel.cancelAlarm() }
        )

        // Sleep Time Section
        SleepTimeSection(
            sleepTimeFormatted = sleepTimeFormatted,
            onSetSleepTimeClick = { showSleepTimePicker = true },
            onClearSleepTimeClick = { homeViewModel.clearSleepTime() }
        )

        // Tracking Section
        TrackingSection(
            isTracking = isTracking,
            onStartTrackingClick = { homeViewModel.startTrackingNow(context) },
            onStopTrackingClick = { homeViewModel.stopTracking(context) }
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
                    homeViewModel.getExactAlarmPermissionIntent()?.let { intent ->
                        context.startActivity(intent)
                    }
                    showPermissionDialog = false
                }) { Text("Grant Permission") }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Alarm Time Picker Dialog
    if (showAlarmTimePicker) {
        TimePickerDialog(
            initialHour = 7,
            initialMinute = 30,
            onTimeSelected = { selectedHour, selectedMinute ->
                homeViewModel.setAlarm(selectedHour, selectedMinute, useSmartAlarm = false)
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
                showSleepTimePicker = false
            },
            onDismiss = { showSleepTimePicker = false }
        )
    }
}

/**
 * Displays a section for managing the alarm.
 */
@Composable
fun AlarmSection(
    alarmTimeFormatted: String,
    onSetAlarmClick: () -> Unit,
    onCancelAlarmClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = "Alarm Time: $alarmTimeFormatted", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onSetAlarmClick) { Text(text = "Set Alarm") }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onCancelAlarmClick) { Text(text = "Cancel Alarm") }
    }
}

/**
 * Displays a section for setting and clearing sleep time.
 */
@Composable
fun SleepTimeSection(
    sleepTimeFormatted: String,
    onSetSleepTimeClick: () -> Unit,
    onClearSleepTimeClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = "Sleep Time: $sleepTimeFormatted", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onSetSleepTimeClick) { Text(text = "Set Sleep Time") }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onClearSleepTimeClick) { Text(text = "Clear Sleep Time") }
    }
}

/**
 * Displays a section for managing sleep tracking.
 */
@Composable
fun TrackingSection(
    isTracking: Boolean,
    onStartTrackingClick: () -> Unit,
    onStopTrackingClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        if (!isTracking) {
            Button(onClick = onStartTrackingClick) { Text(text = "Start Tracking Now") }
        } else {
            Button(onClick = onStopTrackingClick) { Text(text = "Stop Tracking") }
        }
    }
}
