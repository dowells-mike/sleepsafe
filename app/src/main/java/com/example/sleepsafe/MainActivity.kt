package com.example.sleepsafe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.sleepsafe.components.BottomNavigationBar
import com.example.sleepsafe.components.NavHostContainer
import com.example.sleepsafe.ui.theme.SleepsafeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SleepsafeTheme {
                // Directly use Scaffold with NavHostContainer
                MainScaffold(this)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold(activity: ComponentActivity) {
    val navController = rememberNavController()

    // Observe the current route
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "SleepSafe", fontSize = 20.sp) }
            )
        },
        bottomBar = {
            // Show BottomNavigationBar only for main app routes
            if (currentRoute != "welcome") {
                BottomNavigationBar(navController = navController)
            }
        }
    ) { innerPadding ->
        NavHostContainer(
            navController = navController,
            activity = activity,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

