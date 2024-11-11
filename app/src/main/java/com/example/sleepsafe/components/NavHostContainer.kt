package com.example.sleepsafe.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.sleepsafe.BottomNavItem
import com.example.sleepsafe.screens.HomeScreen
import com.example.sleepsafe.screens.AnalysisScreen
import com.example.sleepsafe.screens.SettingsScreen

@Composable
fun NavHostContainer(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(navController = navController, startDestination = BottomNavItem.Home.route, modifier = modifier) {
        composable(BottomNavItem.Home.route) { HomeScreen() }
        composable(BottomNavItem.Analysis.route) { AnalysisScreen() }
        composable(BottomNavItem.Settings.route) { SettingsScreen() }
    }
}
