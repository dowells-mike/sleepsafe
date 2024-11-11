package com.example.sleepsafe.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.example.sleepsafe.BottomNavItem

@Composable
fun AccountSettingsIcon(navController: NavHostController) {
    IconButton(onClick = {
        navController.navigate(BottomNavItem.Account.route)
    }) {
        Icon(imageVector = Icons.Default.AccountCircle, contentDescription = "Account Settings")
    }
}
