package com.example.sleepsafe

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

data class BottomNavItem(val route: String, val icon: ImageVector, val label: String)

val BottomNavItems = listOf(
    BottomNavItem("home", Icons.Default.Home, "Home"),
    BottomNavItem("analysis", Icons.Default.Search, "Analysis"),
    BottomNavItem("settings", Icons.Default.Settings, "Settings"),
    BottomNavItem("account", Icons.Default.AccountCircle, "Account")
)
