// AccountSettingsIcon.kt
package com.example.sleepsafe.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.example.sleepsafe.BottomNavItem
import com.example.sleepsafe.BottomNavItems

/**
 * A composable function to display an account settings icon.
 * When clicked, it navigates to the Account Settings screen.
 * @param navController The NavHostController to manage navigation.
 */
@Composable
fun AccountSettingsIcon(navController: NavHostController) {
    IconButton(onClick = {
        // Navigate to the Account Settings screen
        navController.navigate(BottomNavItems[3].route)
    }) {
        Icon(imageVector = Icons.Default.AccountCircle, contentDescription = "Account Settings")
    }
}
