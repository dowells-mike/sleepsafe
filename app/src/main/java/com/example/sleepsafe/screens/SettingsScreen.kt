package com.example.sleepsafe.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sleepsafe.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(settingsViewModel: SettingsViewModel = viewModel()) {
    val motionSensitivity by settingsViewModel.motionSensitivity.observeAsState(5)
    val audioSensitivity by settingsViewModel.audioSensitivity.observeAsState(5)
    val updateInterval by settingsViewModel.updateInterval.observeAsState(5)
    val isDarkTheme by settingsViewModel.isDarkTheme.observeAsState(false)
    val useSmartAlarm by settingsViewModel.useSmartAlarm.observeAsState(true)
    val smartAlarmWindow by settingsViewModel.smartAlarmWindow.observeAsState(30)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Tracking Settings
        Card {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Tracking Settings",
                    style = MaterialTheme.typography.titleLarge
                )

                // Motion Sensitivity
                Column {
                    Text(
                        text = "Motion Sensitivity",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Slider(
                        value = motionSensitivity.toFloat(),
                        onValueChange = { settingsViewModel.setMotionSensitivity(it.toInt()) },
                        valueRange = 1f..10f,
                        steps = 8
                    )
                    Text(
                        text = when(motionSensitivity) {
                            1, 2 -> "Low - Only detect major movements"
                            3, 4 -> "Medium-Low"
                            5, 6 -> "Medium - Balanced detection"
                            7, 8 -> "Medium-High"
                            else -> "High - Detect slight movements"
                        },
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                // Audio Sensitivity
                Column {
                    Text(
                        text = "Audio Sensitivity",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Slider(
                        value = audioSensitivity.toFloat(),
                        onValueChange = { settingsViewModel.setAudioSensitivity(it.toInt()) },
                        valueRange = 1f..10f,
                        steps = 8
                    )
                    Text(
                        text = when(audioSensitivity) {
                            1, 2 -> "Low - Only detect loud noises"
                            3, 4 -> "Medium-Low"
                            5, 6 -> "Medium - Balanced detection"
                            7, 8 -> "Medium-High"
                            else -> "High - Detect quiet sounds"
                        },
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                // Update Interval
                Column {
                    Text(
                        text = "Data Collection Interval",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Slider(
                        value = updateInterval.toFloat(),
                        onValueChange = { settingsViewModel.setUpdateInterval(it.toInt()) },
                        valueRange = 1f..30f,
                        steps = 28
                    )
                    Text(
                        text = "$updateInterval seconds",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        // Smart Alarm Settings
        Card {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Smart Alarm",
                    style = MaterialTheme.typography.titleLarge
                )

                // Smart Alarm Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Use Smart Alarm",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Switch(
                        checked = useSmartAlarm,
                        onCheckedChange = { settingsViewModel.setUseSmartAlarm(it) }
                    )
                }

                if (useSmartAlarm) {
                    // Smart Alarm Window
                    Column {
                        Text(
                            text = "Wake-up Window",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Slider(
                            value = smartAlarmWindow.toFloat(),
                            onValueChange = { settingsViewModel.setSmartAlarmWindow(it.toInt()) },
                            valueRange = 5f..45f,
                            steps = 7
                        )
                        Text(
                            text = "$smartAlarmWindow minutes before alarm",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }

        // App Settings
        Card {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "App Settings",
                    style = MaterialTheme.typography.titleLarge
                )

                // Theme Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Dark Theme",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Switch(
                        checked = isDarkTheme,
                        onCheckedChange = { settingsViewModel.setDarkTheme(it) }
                    )
                }

                // Data Management
                Button(
                    onClick = { settingsViewModel.clearAllData() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Outlined.Delete, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Clear All Sleep Data")
                }
            }
        }
    }
}
