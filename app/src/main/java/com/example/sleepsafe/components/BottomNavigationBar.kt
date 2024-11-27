// BottomNavigationBar.kt
package com.example.sleepsafe.components

import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.sleepsafe.BottomNavItem
import com.example.sleepsafe.BottomNavItems

/**
 * A composable function to display the bottom navigation bar in the SleepSafe app.
 * @param navController The NavHostController to manage navigation between screens.
 */
@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val items = BottomNavItems

    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        // Iterate through each bottom navigation item and set up the NavigationBarItem
        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                selected = currentRoute == item.route,
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    }
}
