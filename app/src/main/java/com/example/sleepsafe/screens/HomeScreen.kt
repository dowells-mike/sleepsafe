package com.example.sleepsafe.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sleepsafe.viewmodel.HomeViewModel

@Composable
fun HomeScreen(homeViewModel: HomeViewModel = viewModel()) {
    // Observing LiveData properties as Compose states
    val isRecording by homeViewModel.isRecording.observeAsState(false)
    val audioFilePath by homeViewModel.audioFilePath.observeAsState()
    val motionState by homeViewModel.motionState.observeAsState("No Movement")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "SleepSafe")
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Motion: $motionState")
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = if (isRecording) "Recording Audio..." else "Audio Recording Stopped")
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Audio File: ${audioFilePath ?: "No file"}")

        Spacer(modifier = Modifier.height(16.dp))
        if (isRecording) {
            Button(onClick = { homeViewModel.stopAudioRecording() }) {
                Text(text = "Stop Audio Recording")
            }
        } else {
            Button(onClick = { homeViewModel.startAudioRecording() }) {
                Text(text = "Start Audio Recording")
            }
        }
    }
}
