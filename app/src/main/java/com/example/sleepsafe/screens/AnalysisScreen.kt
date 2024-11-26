// AnalysisScreen.kt
package com.example.sleepsafe.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sleepsafe.data.SleepData
import com.example.sleepsafe.viewmodel.AnalysisViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AnalysisScreen(analysisViewModel: AnalysisViewModel = viewModel()) {
    val sleepData by analysisViewModel.sleepData.observeAsState()
    val selectedDate by analysisViewModel.selectedDate.observeAsState(Date())

    if (sleepData == null || sleepData!!.isEmpty()) {
        // No data available
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("No sleep data available for the selected date.")
        }
    } else {
        // Display sleep insights
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Date selector
            DateSelector(selectedDate) { date ->
                analysisViewModel.updateSelectedDate(date)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sleep Quality Percentage
            SleepQualityView(sleepData!!)

            Spacer(modifier = Modifier.height(16.dp))

            // Sleep Graph
            SleepGraphView(sleepData!!)

            Spacer(modifier = Modifier.height(16.dp))

            // Snore Analysis
            SnoreAnalysisView(sleepData!!)

            Spacer(modifier = Modifier.height(16.dp))

            // Additional Data
            SleepAdditionalDataView(sleepData!!)
        }
    }
}

@Composable
fun DateSelector(selectedDate: Date, onDateSelected: (Date) -> Unit) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = {
            val calendar = Calendar.getInstance().apply { time = selectedDate }
            calendar.add(Calendar.DAY_OF_YEAR, -1)
            onDateSelected(calendar.time)
        }) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous Day")
        }
        Text(text = dateFormat.format(selectedDate))
        IconButton(onClick = {
            val calendar = Calendar.getInstance().apply { time = selectedDate }
            calendar.add(Calendar.DAY_OF_YEAR, 1)
            onDateSelected(calendar.time)
        }) {
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Day")
        }
    }
}

@Composable
fun SleepQualityView(sleepData: List<SleepData>) {
    // Calculate sleep quality
    val sleepQuality = calculateSleepQuality(sleepData)
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Circular progress bar
        CircularProgressIndicator(
            progress = { sleepQuality / 100f },
            modifier = Modifier.size(100.dp),
            strokeWidth = 8.dp,
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text("Time in Bed: ${formatDuration(sleepData)}")
            Text("Time Asleep: ${formatDuration(sleepData)}") // Adjust as needed
        }
    }
}

@Composable
fun SleepGraphView(sleepData: List<SleepData>) {
    // Implement a graph using your preferred charting library
    Text("Sleep Graph (Implementation Pending)")
}

@Composable
fun SnoreAnalysisView(sleepData: List<SleepData>) {
    // Implement snore audio spectrum
    Text("Snore Analysis (Implementation Pending)")
}

@Composable
fun SleepAdditionalDataView(sleepData: List<SleepData>) {
    val wentToBedAt = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(sleepData.first().timestamp))
    val wokeUpAt = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(sleepData.last().timestamp))
    val asleepAfter = "N/A" // Implement calculation if possible
    val snoreTime = calculateSnoreTime(sleepData)

    Column {
        Text("Snore: $snoreTime minutes")
        Text("Went to bed at: $wentToBedAt")
        Text("Woke up at: $wokeUpAt")
        Text("Asleep after: $asleepAfter")
    }
}

fun calculateSleepQuality(sleepData: List<SleepData>): Int {
    // Implement your algorithm to calculate sleep quality
    return 80 // Placeholder value
}

fun formatDuration(sleepData: List<SleepData>): String {
    val durationMillis = sleepData.last().timestamp - sleepData.first().timestamp
    val hours = (durationMillis / (1000 * 60 * 60)).toInt()
    val minutes = ((durationMillis / (1000 * 60)) % 60).toInt()
    return "${hours}h ${minutes}m"
}

fun calculateSnoreTime(sleepData: List<SleepData>): Int {
    // Implement your algorithm to calculate snore time
    return 10 // Placeholder value
}
