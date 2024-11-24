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
        Text(text = "Set an Alarm", style = androidx.compose.material3.MaterialTheme.typography.titleLarge)
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
    }
}
