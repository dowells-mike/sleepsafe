// AnalysisScreen.kt
package com.example.sleepsafe.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sleepsafe.data.SleepData
import com.example.sleepsafe.viewmodel.AnalysisViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AnalysisScreen(analysisViewModel: AnalysisViewModel = viewModel()) {
    val sleepData by analysisViewModel.sleepData.observeAsState(emptyList())
    val availableSessions by analysisViewModel.availableSessions.observeAsState(emptyList())

    // Auto-refresh effect
    LaunchedEffect(Unit) {
        analysisViewModel.startAutoRefresh()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        if (sleepData.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No sleep data available.\nStart sleep tracking to see your data.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            // Current Session Info
            CurrentSessionInfo(sleepData.first(), sleepData.last())
            Spacer(modifier = Modifier.height(16.dp))

            // Sleep Patterns Section
            SleepPatternsView(sleepData)
            Spacer(modifier = Modifier.height(16.dp))

            // Latest Readings
            LatestReadingsView(sleepData.lastOrNull())
            Spacer(modifier = Modifier.height(16.dp))

            // Session History
            SessionHistoryView(
                availableSessions = availableSessions,
                onSessionSelected = { analysisViewModel.loadSleepSession(it) }
            )
        }
    }
}

@Composable
fun CurrentSessionInfo(firstReading: SleepData, lastReading: SleepData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "Current Session",
                style = MaterialTheme.typography.titleLarge
            )

            val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            val startTime = Date(firstReading.sleepStart)
            val currentDuration = lastReading.timestamp - firstReading.sleepStart

            Text("Started: ${timeFormat.format(startTime)}")
            Text("Duration: ${formatDurationWithSeconds(currentDuration)}")
            if (lastReading.alarmTime > 0) {
                val remainingTime = lastReading.alarmTime - lastReading.timestamp
                Text("Time until alarm: ${formatDurationWithSeconds(remainingTime)}")
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
            Text(
                "Max Motion: ${sleepData.maxOfOrNull { it.motion }?.format(2) ?: "0.00"}",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End
            )

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
            Text(
                "Max Noise: ${sleepData.maxOfOrNull { it.audioLevel }?.format(2) ?: "0.00"}",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End
            )
        }
    }
}

@Composable
fun LatestReadingsView(latestData: SleepData?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "Latest Readings",
                style = MaterialTheme.typography.titleLarge
            )

            if (latestData != null) {
                val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                Text("Time: ${timeFormat.format(Date(latestData.timestamp))}")
                Text("Motion: ${latestData.motion.format(2)}")
                Text("Noise: ${latestData.audioLevel.format(2)}")
                Text("Sleep Phase: ${latestData.sleepPhase}")
            } else {
                Text("No readings available")
            }
        }
    }
}

@Composable
fun SessionHistoryView(
    availableSessions: List<Long>,
    onSessionSelected: (Long) -> Unit
) {
    if (availableSessions.isNotEmpty()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "Session History",
                    style = MaterialTheme.typography.titleLarge
                )

                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    availableSessions.forEach { sessionStart ->
                        val sessionDate = Date(sessionStart)
                        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                        FilterChip(
                            selected = false,
                            onClick = { onSessionSelected(sessionStart) },
                            label = { Text(timeFormat.format(sessionDate)) }
                        )
                    }
                }
            }
        }
    }
}

private fun Float.format(decimals: Int) = "%.${decimals}f".format(this)

@SuppressLint("DefaultLocale")
private fun formatDurationWithSeconds(millis: Long): String {
    val hours = millis / (1000 * 60 * 60)
    val minutes = (millis % (1000 * 60 * 60)) / (1000 * 60)
    val seconds = (millis % (1000 * 60)) / 1000
    return String.format("%02d:%02d:%02d", hours, minutes, seconds)
}
