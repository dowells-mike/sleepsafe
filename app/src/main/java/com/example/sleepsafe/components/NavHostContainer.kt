package com.example.sleepsafe.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.sleepsafe.BottomNavItem
import com.example.sleepsafe.BottomNavItems
import com.example.sleepsafe.screens.HomeScreen
import com.example.sleepsafe.screens.AnalysisScreen
import com.example.sleepsafe.screens.SettingsScreen
import com.example.sleepsafe.screens.AccountScreen

@Composable
fun NavHostContainer(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(navController = navController, startDestination = BottomNavItems[0].route, modifier = modifier) {
        composable(BottomNavItems[0].route) { HomeScreen() }
        composable(BottomNavItems[1].route) { AnalysisScreen() }
        composable(BottomNavItems[2].route) { SettingsScreen() }
        composable(BottomNavItems[3].route) { AccountScreen() }
    }
}
