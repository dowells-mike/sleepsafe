// AnalysisScreen.kt
package com.example.sleepsafe.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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

/**
 * Composable function to display the Analysis screen.
 * It provides sleep insights, graphs, and additional sleep-related data.
 *
 * @param analysisViewModel The ViewModel for managing sleep data and selected date.
 */
@Composable
fun AnalysisScreen(analysisViewModel: AnalysisViewModel = viewModel()) {
    val sleepData by analysisViewModel.sleepData.observeAsState()
    val selectedDate by analysisViewModel.selectedDate.observeAsState(Date())

    if (sleepData.isNullOrEmpty()) {
        // Display a message when no sleep data is available
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("No sleep data available for the selected date.")
        }
    } else {
        // Display sleep insights and analysis
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            DateSelector(selectedDate) { date ->
                analysisViewModel.updateSelectedDate(date)
            }
            Spacer(modifier = Modifier.height(16.dp))
            SleepQualityView(sleepData!!)
            Spacer(modifier = Modifier.height(16.dp))
            SleepGraphView(sleepData!!)
            Spacer(modifier = Modifier.height(16.dp))
            SnoreAnalysisView(sleepData!!)
            Spacer(modifier = Modifier.height(16.dp))
            SleepAdditionalDataView(sleepData!!)
        }
    }
}

/**
 * Displays a date selector with navigation for previous and next days.
 *
 * @param selectedDate The currently selected date.
 * @param onDateSelected Callback to update the selected date.
 */
@Composable
fun DateSelector(selectedDate: Date, onDateSelected: (Date) -> Unit) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    Row(verticalAlignment = Alignment.CenterVertically) {
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

/**
 * Displays sleep quality information with a circular progress bar and time stats.
 *
 * @param sleepData The list of SleepData objects.
 */
@Composable
fun SleepQualityView(sleepData: List<SleepData>) {
    val sleepQuality = calculateSleepQuality(sleepData)
    Row(verticalAlignment = Alignment.CenterVertically) {
        CircularProgressIndicator(
            progress = sleepQuality / 100f,
            modifier = Modifier.size(100.dp),
            strokeWidth = 8.dp,
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text("Time in Bed: ${formatDuration(sleepData)}")
            Text("Time Asleep: ${formatDuration(sleepData)}")
        }
    }
}

/**
 * Placeholder for a sleep graph view.
 *
 * @param sleepData The list of SleepData objects.
 */
@Composable
fun SleepGraphView(sleepData: List<SleepData>) {
    Text("Sleep Graph (Implementation Pending)")
}

/**
 * Placeholder for snore analysis view.
 *
 * @param sleepData The list of SleepData objects.
 */
@Composable
fun SnoreAnalysisView(sleepData: List<SleepData>) {
    Text("Snore Analysis (Implementation Pending)")
}

/**
 * Displays additional sleep-related data such as snore time, bed time, and wake-up time.
 *
 * @param sleepData The list of SleepData objects.
 */
@Composable
fun SleepAdditionalDataView(sleepData: List<SleepData>) {
    val wentToBedAt = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(sleepData.first().timestamp))
    val wokeUpAt = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(sleepData.last().timestamp))
    val snoreTime = calculateSnoreTime(sleepData)

    Column {
        Text("Snore: $snoreTime minutes")
        Text("Went to bed at: $wentToBedAt")
        Text("Woke up at: $wokeUpAt")
    }
}

/**
 * Calculates sleep quality as a percentage (placeholder implementation).
 *
 * @param sleepData The list of SleepData objects.
 * @return The calculated sleep quality percentage.
 */
fun calculateSleepQuality(sleepData: List<SleepData>): Int {
    return 80 // Placeholder value
}

/**
 * Formats the duration of time between the first and last sleep data entries.
 *
 * @param sleepData The list of SleepData objects.
 * @return A formatted duration string (e.g., "7h 30m").
 */
fun formatDuration(sleepData: List<SleepData>): String {
    val durationMillis = sleepData.last().timestamp - sleepData.first().timestamp
    val hours = (durationMillis / (1000 * 60 * 60)).toInt()
    val minutes = ((durationMillis / (1000 * 60)) % 60).toInt()
    return "${hours}h ${minutes}m"
}

/**
 * Calculates the total snore time in minutes (placeholder implementation).
 *
 * @param sleepData The list of SleepData objects.
 * @return The calculated snore time in minutes.
 */
fun calculateSnoreTime(sleepData: List<SleepData>): Int {
    return 10 // Placeholder value
}
