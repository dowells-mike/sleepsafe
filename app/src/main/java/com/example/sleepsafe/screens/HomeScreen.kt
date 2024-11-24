package com.example.sleepsafe.screens

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sleepsafe.viewmodel.HomeViewModel
import android.widget.Toast
import androidx.compose.material3.*
import androidx.compose.runtime.*
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

    //Test for recording audio and accelerometer sensor
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(16.dp),
//        horizontalAlignment = Alignment.CenterHorizontally,
//        verticalArrangement = Arrangement.Center
//    ) {
//        Text(text = "SleepSafe")
//        Spacer(modifier = Modifier.height(16.dp))
//        Text(text = "Motion: $motionState")
//        Spacer(modifier = Modifier.height(16.dp))
//        Text(text = if (isRecording) "Recording Audio..." else "Audio Recording Stopped")
//        Spacer(modifier = Modifier.height(16.dp))
//        Text(text = "Audio File: ${audioFilePath ?: "No file"}")
//
//        Spacer(modifier = Modifier.height(16.dp))
//        if (isRecording) {
//            Button(onClick = { homeViewModel.stopAudioRecording() }) {
//                Text(text = "Stop Audio Recording")
//            }
//        } else {
//            Button(onClick = { homeViewModel.startAudioRecording() }) {
//                Text(text = "Start Audio Recording")
//            }
//        }
//    }

    // Layout for alarm
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Set Alarm")
        Spacer(modifier = Modifier.height(16.dp))

        // Display current selected time
        Text(
            text = if (hour == 0 && minute == 0) {
                "No time selected"
            } else {
                "Selected Time: %02d:%02d".format(hour, minute)
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Button to open the time picker
        Button(onClick = { showTimePicker = true }) {
            Text("Pick Time")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Time picker dialog
        if (showTimePicker) {
            TimePickerDialog(
                initialHour = hour,
                initialMinute = minute,
                onTimeSelected = { selectedHour, selectedMinute ->
                    hour = selectedHour
                    minute = selectedMinute
                    showTimePicker = false
                },
                onDismiss = { showTimePicker = false }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (!homeViewModel.hasExactAlarmPermission()) {
            Text(text = "Permission required to set exact alarms.")
            Button(onClick = {
                homeViewModel.requestExactAlarmPermission()
            }) {
                Text("Grant Permission")
            }
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Button(onClick = {
                    homeViewModel.setAlarm(hour, minute, useSmartAlarm)
                    Toast.makeText(
                        homeViewModel.getApplication(),
                        "Alarm set for %02d:%02d".format(hour, minute),
                        Toast.LENGTH_SHORT
                    ).show()
                }) {
                    Text("Set Alarm")
                }
                Spacer(modifier = Modifier.width(16.dp))
                Button(onClick = {
                    homeViewModel.cancelAlarm()
                    Toast.makeText(
                        homeViewModel.getApplication(),
                        "Alarm Canceled",
                        Toast.LENGTH_SHORT
                    ).show()
                }) {
                    Text("Cancel Alarm")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        if (alarmTime != null) {
            Text(
                text = "Alarm Set for: ${
                    Calendar.getInstance().apply { timeInMillis = alarmTime!! }.time
                }"
            )
        }
    }
}
