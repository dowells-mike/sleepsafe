// AnalysisScreen.kt
package com.example.sleepsafe.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sleepsafe.data.SleepData
import com.example.sleepsafe.data.SleepQualityMetrics
import com.example.sleepsafe.data.SleepSessionSummary
import com.example.sleepsafe.viewmodel.AnalysisViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * Composable function to display the Analysis screen.
 * It provides sleep insights, graphs, and additional sleep-related data.
 */

@Composable
fun AnalysisScreen(analysisViewModel: AnalysisViewModel = viewModel()) {
    val sleepData by analysisViewModel.sleepData.observeAsState(emptyList())
    val selectedDate by analysisViewModel.selectedDate.observeAsState(Date())
    val sleepQuality by analysisViewModel.sleepQuality.observeAsState()
    val sessionSummary by analysisViewModel.sessionSummary.observeAsState()
    val availableSessions by analysisViewModel.availableSessions.observeAsState(emptyList())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Date and Session Selection
        DateSelector(
            selectedDate = selectedDate,
            availableSessions = availableSessions,
            onDateSelected = { analysisViewModel.updateSelectedDate(it) },
            onSessionSelected = { analysisViewModel.loadSleepSession(it) }
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (sleepData.isEmpty()) {
            NoDataView()
        } else {
            // Sleep Quality Section
            sleepQuality?.let { metrics ->
                SleepQualityView(metrics, sessionSummary)
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Sleep Patterns Section
            if (sleepData.isNotEmpty()) {
                SleepPatternsView(sleepData)
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Detailed Analysis
            sleepQuality?.let { metrics ->
                DetailedAnalysisView(metrics, analysisViewModel.getSleepQualityTrend())
            }
        }
    }
}


@Composable
fun DateSelector(
    selectedDate: Date,
    availableSessions: List<Long>,
    onDateSelected: (Date) -> Unit,
    onSessionSelected: (Long) -> Unit
) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    Column {
        // Date Navigation
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                val calendar = Calendar.getInstance().apply { time = selectedDate }
                calendar.add(Calendar.DAY_OF_YEAR, -1)
                onDateSelected(calendar.time)
            }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Previous Day")
            }
            Text(
                text = dateFormat.format(selectedDate),
                style = MaterialTheme.typography.titleMedium
            )
            IconButton(onClick = {
                val calendar = Calendar.getInstance().apply { time = selectedDate }
                calendar.add(Calendar.DAY_OF_YEAR, 1)
                onDateSelected(calendar.time)
            }) {
                Icon(Icons.AutoMirrored.Filled.ArrowForward, "Next Day")
            }
        }

        // Session Selection
        if (availableSessions.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Available Sessions:",
                style = MaterialTheme.typography.labelMedium
            )
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                availableSessions.forEach { sessionStart ->
                    val sessionDate = Date(sessionStart)
                    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                    FilterChip(
                        selected = sessionStart == selectedDate.time,
                        onClick = { onSessionSelected(sessionStart) },
                        label = { Text(timeFormat.format(sessionDate)) }
                    )
                }
            }
        }
    }
}

@Composable
fun NoDataView() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            "No sleep data available for the selected date.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun SleepQualityView(metrics: SleepQualityMetrics, summary: SleepSessionSummary?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "Sleep Quality",
                style = MaterialTheme.typography.titleLarge
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Quality Score
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "${metrics.calculateQualityScore()}",
                        style = MaterialTheme.typography.displayMedium
                    )
                    Text(
                        metrics.getQualitativeAssessment(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Summary Stats
                Column {
                    summary?.let {
                        Text("Average Motion: ${it.avgMotion.format(2)}")
                        Text("Average Noise: ${it.avgAudioLevel.format(2)}")
                        Text("Duration: ${formatDuration(it.timestamp, it.alarmTime)}")
                    }
                }
            }
        }
    }
}

@Composable
fun SleepPatternsView(sleepData: List<SleepData>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "Sleep Patterns",
                style = MaterialTheme.typography.titleLarge
            )

            // Motion Pattern
            Text("Motion Pattern", fontWeight = FontWeight.Bold)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                val maxMotion = sleepData.maxOfOrNull { it.motion } ?: 1f
                sleepData.forEach { data ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight((data.motion / maxMotion).coerceIn(0f, 1f))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.7f))
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Audio Pattern
            Text("Noise Pattern", fontWeight = FontWeight.Bold)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                val maxAudio = sleepData.maxOfOrNull { it.audioLevel } ?: 1f
                sleepData.forEach { data ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight((data.audioLevel / maxAudio).coerceIn(0f, 1f))
                            .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f))
                    )
                }
            }
        }
    }
}

@Composable
fun DetailedAnalysisView(metrics: SleepQualityMetrics, trend: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "Detailed Analysis",
                style = MaterialTheme.typography.titleLarge
            )

            Text(trend)
        }
    }
}

private fun Float.format(decimals: Int) = "%.${decimals}f".format(this)

private fun formatDuration(startTime: Long, endTime: Long): String {
    if (endTime <= startTime) return "Unknown"
    val durationMillis = endTime - startTime
    val hours = durationMillis / (1000 * 60 * 60)
    val minutes = (durationMillis % (1000 * 60 * 60)) / (1000 * 60)
    return String.format("%dh %dm", hours, minutes)
}
