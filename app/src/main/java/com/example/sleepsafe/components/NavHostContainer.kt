// NavHostContainer.kt
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

/**
 * A composable function to manage the navigation graph of the SleepSafe app.
 * @param navController The NavHostController to control navigation between composables.
 * @param activity The ComponentActivity instance to manage activity lifecycle events.
 * @param modifier A Modifier to customize the layout or appearance of this composable.
 */
@Composable
fun NavHostContainer(
    navController: NavHostController,
    activity: ComponentActivity,
    modifier: Modifier = Modifier
) {
    // Define the navigation graph
    NavHost(navController = navController, startDestination = "welcome", modifier = modifier) {
        // Welcome Screen as the entry point
        composable("welcome") { WelcomeScreen(navController = navController, activity = activity) }

        // Define navigation for each bottom navigation item
        composable(BottomNavItems[0].route) { HomeScreen() }
        composable(BottomNavItems[1].route) { AnalysisScreen() }
        composable(BottomNavItems[2].route) { SettingsScreen() }
        composable(BottomNavItems[3].route) { AccountScreen() }
    }
}
