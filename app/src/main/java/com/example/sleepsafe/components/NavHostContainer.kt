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
fun NavHostContainer(
    navController: NavHostController,
    activity: ComponentActivity? = null,
    modifier: Modifier = Modifier
) {
    NavHost(navController = navController, startDestination = "welcome", modifier = modifier) {
        composable("welcome") {
            WelcomeScreen(navController = navController)
        }
        composable("home") {
            HomeScreen()
        }
        composable("analysis") {
            AnalysisScreen()
        }
        composable("settings") {
            SettingsScreen()
        }
        composable("account") {
            AccountScreen()
        }
    }
}
