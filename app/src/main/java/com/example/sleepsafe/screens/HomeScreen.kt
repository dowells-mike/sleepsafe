// HomeScreen.kt
package com.example.sleepsafe.screens

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ShowChart
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sleepsafe.utils.SleepTracker
import com.example.sleepsafe.viewmodel.HomeViewModel
import com.example.sleepsafe.data.SleepQualityMetrics
import java.text.SimpleDateFormat
import java.util.*

@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun HomeScreen(homeViewModel: HomeViewModel = viewModel()) {
    val context = LocalContext.current
    val sleepTime by homeViewModel.sleepTime.observeAsState()
    val isTracking by homeViewModel.isTracking.observeAsState(false)
    val alarmTime by homeViewModel.alarmTime.observeAsState()
    val permissionRequired by homeViewModel.permissionRequired.observeAsState(false)
    val currentPhase by homeViewModel.currentSleepPhase.observeAsState(SleepTracker.SleepPhase.AWAKE)
    val sleepQuality by homeViewModel.lastSleepQuality.observeAsState()
    val sleepDuration by homeViewModel.currentSleepDuration.observeAsState(0L)

    var showAlarmTimePicker by remember { mutableStateOf(false) }
    var showQuickAlarmPicker by remember { mutableStateOf(false) }
    var showSleepTimePicker by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Status Card
            SleepStatusCard(
                isTracking = isTracking,
                currentPhase = currentPhase,
                sleepDuration = sleepDuration,
                alarmTime = alarmTime
            )

            // Quick Actions
            QuickActionsRow(
                isTracking = isTracking,
                onStartTracking = {
                    if (!isTracking) {
                        showQuickAlarmPicker = true
                    }
                },
                onStopTracking = { homeViewModel.stopTracking(context) }
            )

            // Time Settings
            TimeSettingsCard(
                sleepTime = sleepTime,
                alarmTime = alarmTime,
                onSetSleepTime = { showSleepTimePicker = true },
                onSetAlarmTime = { showAlarmTimePicker = true }
            )

            // Last Sleep Insights
            sleepQuality?.let { quality ->
                SleepInsightsCard(quality)
            }

            // Sleep Tips
            SleepTipsCard()
        }

        // Floating Action Button for quick start/stop
        FloatingActionButton(
            onClick = {
                if (isTracking) {
                    homeViewModel.stopTracking(context)
                } else {
                    showQuickAlarmPicker = true
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = if (isTracking)
                MaterialTheme.colorScheme.error
            else
                MaterialTheme.colorScheme.primary
        ) {
            Icon(
                imageVector = if (isTracking) Icons.Filled.Stop else Icons.Filled.PlayArrow,
                contentDescription = if (isTracking) "Stop Tracking" else "Start Tracking"
            )
        }
    }

    // Time Pickers
    if (showAlarmTimePicker) {
        TimePickerDialog(
            initialHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
            initialMinute = Calendar.getInstance().get(Calendar.MINUTE),
            onTimeSelected = { hour, minute ->
                val calendar = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, hour)
                    set(Calendar.MINUTE, minute)
                    // If selected time is before current time, add one day
                    if (timeInMillis <= System.currentTimeMillis()) {
                        add(Calendar.DAY_OF_YEAR, 1)
                    }
                }
                homeViewModel.setAlarm(calendar.timeInMillis)
            },
            onDismiss = { showAlarmTimePicker = false }
        )
    }

    if (showQuickAlarmPicker) {
        QuickTimePickerDialog(
            onTimeSelected = { timestamp ->
                if (permissionRequired) {
                    homeViewModel.getExactAlarmPermissionIntent()?.let { intent ->
                        context.startActivity(intent)
                    }
                } else {
                    homeViewModel.startTrackingWithAlarm(context, timestamp)
                }
                showQuickAlarmPicker = false
            },
            onDismiss = { showQuickAlarmPicker = false }
        )
    }

    if (showSleepTimePicker) {
        TimePickerDialog(
            initialHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
            initialMinute = Calendar.getInstance().get(Calendar.MINUTE),
            onTimeSelected = { hour, minute ->
                val calendar = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, hour)
                    set(Calendar.MINUTE, minute)
                }
                homeViewModel.setSleepTime(calendar)
            },
            onDismiss = { showSleepTimePicker = false }
        )
    }
}

@Composable
fun SleepStatusCard(
    isTracking: Boolean,
    currentPhase: SleepTracker.SleepPhase,
    sleepDuration: Long,
    alarmTime: Long?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Sleep Phase Indicator
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = when (currentPhase) {
                            SleepTracker.SleepPhase.DEEP_SLEEP -> Icons.Outlined.DarkMode
                            SleepTracker.SleepPhase.LIGHT_SLEEP -> Icons.Outlined.Brightness3
                            SleepTracker.SleepPhase.REM -> Icons.Outlined.Visibility
                            else -> Icons.Outlined.LightMode
                        },
                        contentDescription = "Sleep Phase",
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = currentPhase.name.replace("_", " "),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Duration and Time Left
            if (isTracking) {
                Text(
                    text = formatDuration(sleepDuration),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold
                )

                alarmTime?.let {
                    val timeLeft = it - System.currentTimeMillis()
                    if (timeLeft > 0) {
                        Text(
                            text = "${formatDuration(timeLeft)} until alarm",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                Text(
                    text = "Not tracking",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun QuickActionsRow(
    isTracking: Boolean,
    onStartTracking: () -> Unit,
    onStopTracking: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        QuickActionButton(
            icon = Icons.Outlined.Brightness3,
            label = "Sleep Now",
            enabled = !isTracking,
            onClick = onStartTracking
        )
        QuickActionButton(
            icon = Icons.Filled.Stop,
            label = "Stop",
            enabled = isTracking,
            onClick = onStopTracking
        )
        QuickActionButton(
            icon = Icons.AutoMirrored.Outlined.ShowChart,
            label = "Analysis",
            enabled = true,
            onClick = { /* Navigate to analysis */ }
        )
    }
}

@Composable
fun QuickActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(8.dp)
    ) {
        FilledTonalIconButton(
            onClick = onClick,
            enabled = enabled,
            modifier = Modifier.size(56.dp)
        ) {
            Icon(icon, contentDescription = label)
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (enabled)
                MaterialTheme.colorScheme.onSurface
            else
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

@Composable
fun TimeSettingsCard(
    sleepTime: Calendar?,
    alarmTime: Long?,
    onSetSleepTime: () -> Unit,
    onSetAlarmTime: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Schedule",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TimeSettingItem(
                    icon = Icons.Outlined.Brightness3,
                    label = "Bedtime",
                    time = sleepTime?.let { formatTime(it.time) } ?: "Not set",
                    onClick = onSetSleepTime
                )

                TimeSettingItem(
                    icon = Icons.Outlined.LightMode,
                    label = "Wake up",
                    time = alarmTime?.let { formatTime(Date(it)) } ?: "Not set",
                    onClick = onSetAlarmTime
                )
            }
        }
    }
}

@Composable
fun TimeSettingItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    time: String,
    onClick: () -> Unit
) {
    OutlinedCard(
        onClick = onClick,
        modifier = Modifier.padding(4.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(icon, contentDescription = label)
            Column {
                Text(text = label, style = MaterialTheme.typography.labelMedium)
                Text(text = time, style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@Composable
fun SleepInsightsCard(quality: SleepQualityMetrics) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Last Night's Sleep",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                InsightItem(
                    value = quality.calculateQualityScore().toString(),
                    label = "Quality",
                    suffix = "%"
                )
                InsightItem(
                    value = quality.avgMotion.format(1),
                    label = "Movement"
                )
                InsightItem(
                    value = quality.avgAudio.format(1),
                    label = "Noise"
                )
            }
        }
    }
}

@Composable
fun InsightItem(value: String, label: String, suffix: String = "") {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "$value$suffix",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun SleepTipsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Sleep Tips",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            ListItem(
                headlineContent = { Text("Maintain a consistent schedule") },
                leadingContent = {
                    Icon(Icons.Outlined.Schedule, contentDescription = null)
                }
            )

            ListItem(
                headlineContent = { Text("Avoid screens before bed") },
                leadingContent = {
                    Icon(Icons.Outlined.PhoneIphone, contentDescription = null)
                }
            )

            ListItem(
                headlineContent = { Text("Keep room cool and dark") },
                leadingContent = {
                    Icon(Icons.Outlined.AcUnit, contentDescription = null)
                }
            )
        }
    }
}

private fun formatTime(date: Date): String {
    return SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
}

@SuppressLint("DefaultLocale")
private fun formatDuration(millis: Long): String {
    val hours = millis / (1000 * 60 * 60)
    val minutes = (millis % (1000 * 60 * 60)) / (1000 * 60)
    return String.format("%dh %02dm", hours, minutes)
}

private fun Float.format(decimals: Int): String = "%.${decimals}f".format(this)
