package com.example.sleepsafe.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable

@Composable
fun AccountSettingsIcon() {
    IconButton(onClick = {
        // Handle settings icon click - navigate to account or settings
    }) {
        Icon(imageVector = Icons.Default.AccountCircle, contentDescription = "Account Settings")
    }
}
