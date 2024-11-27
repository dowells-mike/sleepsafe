// SettingsScreen.kt
package com.example.sleepsafe.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sleepsafe.viewmodel.SettingsViewModel

/**
 * Displays the Settings screen, where users can view and modify app settings.
 *
 * @param settingsViewModel The ViewModel for managing settings-related logic and state.
 *                          Defaults to the ViewModel provided by the lifecycle.
 */
@Composable
fun SettingsScreen(settingsViewModel: SettingsViewModel = viewModel()) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Display the title of the Settings screen
        Text(
            text = "Settings Screen",
            fontSize = 24.sp
        )
    }
}
