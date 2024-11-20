package com.example.sleepsafe.components

import WelcomeScreen
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.sleepsafe.BottomNavItem
import com.example.sleepsafe.BottomNavItems
import com.example.sleepsafe.screens.*

@Composable
fun NavHostContainer(navController: NavHostController, activity: ComponentActivity, modifier: Modifier = Modifier) {
    NavHost(navController = navController, startDestination = "welcome", modifier = modifier) {
        // Welcome Screen as the entry point
        composable("welcome") { WelcomeScreen(navController = navController, activity = activity) }

        // Bottom navigation setup after the user enters the app
        composable(BottomNavItems[0].route) { HomeScreen() }
        composable(BottomNavItems[1].route) { AnalysisScreen() }
        composable(BottomNavItems[2].route) { SettingsScreen() }
        composable(BottomNavItems[3].route) { AccountScreen() }
    }
}
