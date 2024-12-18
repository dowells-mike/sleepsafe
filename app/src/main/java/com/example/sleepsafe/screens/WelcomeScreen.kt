package com.example.sleepsafe.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.sleepsafe.utils.PermissionsHelper

/**
 * Displays the Welcome screen with an introduction to the app's features and a "Get Started" button.
 *
 * @param navController The NavController for navigating between screens.
 * @param permissionsHelper The PermissionsHelper instance to handle permissions.
 */
@Composable
fun WelcomeScreen(
    navController: NavController,
    permissionsHelper: PermissionsHelper
) {
    var showRuntimePermissionDialog by remember { mutableStateOf(false) }
    var showExactAlarmDialog by remember { mutableStateOf(false) }

    // Pager state to manage the current page in the horizontal pager
    val pagerState = rememberPagerState(pageCount = { 3 }) // Number of pages in the pager

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Horizontal pager for swiping through feature pages
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f) // Take up remaining space
        ) { page ->
            when (page) {
                0 -> FeaturePage("Welcome to SleepSafe", "Track your sleep effortlessly!")
                1 -> FeaturePage("Sleep Analytics", "Understand your sleep patterns.")
                2 -> FeaturePage("Smart Alarms", "Wake up refreshed!")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                when {
                    permissionsHelper.getMissingRuntimePermissions().isNotEmpty() -> {
                        showRuntimePermissionDialog = true
                    }
                    permissionsHelper.needsExactAlarmPermission() -> {
                        showExactAlarmDialog = true
                    }
                    else -> {
                        navController.navigate("home")
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Get Started")
        }

        // Runtime Permissions Dialog
        if (showRuntimePermissionDialog) {
            val missingPermissions = permissionsHelper.getMissingRuntimePermissions()
            AlertDialog(
                onDismissRequest = { showRuntimePermissionDialog = false },
                title = { Text("Permissions Required") },
                text = {
                    Column {
                        Text("SleepSafe needs the following permissions to function properly:")
                        missingPermissions.forEach { permission ->
                            Text("• ${permissionsHelper.getPermissionExplanation(permission)}")
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        permissionsHelper.requestRuntimePermissions { granted ->
                            showRuntimePermissionDialog = false
                            if (granted) {
                                if (permissionsHelper.needsExactAlarmPermission()) {
                                    showExactAlarmDialog = true
                                } else {
                                    navController.navigate("home")
                                }
                            }
                        }
                    }) {
                        Text("Grant Permissions")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showRuntimePermissionDialog = false }) {
                        Text("Later")
                    }
                }
            )
        }

        // Exact Alarm Permission Dialog
        if (showExactAlarmDialog) {
            AlertDialog(
                onDismissRequest = { showExactAlarmDialog = false },
                title = { Text("Additional Permission Required") },
                text = {
                    Column {
                        Text("SleepSafe needs one more permission to ensure alarms work correctly:")
                        Text("• ${permissionsHelper.getPermissionExplanation("android.permission.SCHEDULE_EXACT_ALARM")}")
                        Text("\nThis requires a quick visit to system settings.")
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        showExactAlarmDialog = false
                        permissionsHelper.openExactAlarmSettings()
                    }) {
                        Text("Open Settings")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showExactAlarmDialog = false
                        navController.navigate("home")
                    }) {
                        Text("Skip")
                    }
                }
            )
        }
    }
}

/**
 * A composable function to display individual feature pages in the welcome screen.
 *
 * @param title The title of the feature.
 * @param description The description of the feature.
 */
@Composable
fun FeaturePage(title: String, description: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = title, style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = description, style = MaterialTheme.typography.bodyMedium)
    }
}
