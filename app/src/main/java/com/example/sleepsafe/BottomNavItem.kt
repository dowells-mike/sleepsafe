package com.example.sleepsafe

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(var route: String, var icon: ImageVector, var label: String) {
    object Home : BottomNavItem("home", Icons.Default.Home, "Home")
    object Analysis : BottomNavItem("analysis", Icons.Default.Search, "Analysis")
    object Settings : BottomNavItem("settings", Icons.Default.Settings, "Settings")
    object Account : BottomNavItem("account", Icons.Default.AccountCircle, "Account")
}
