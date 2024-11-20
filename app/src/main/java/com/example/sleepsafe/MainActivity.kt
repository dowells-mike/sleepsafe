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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
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
    // Create and remember a NavController for navigation
    val navController = rememberNavController()

    // Use Scaffold to structure the top bar, content, and bottom navigation
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "SleepSafe", fontSize = 20.sp) }
            )
        },
        bottomBar = {
            BottomNavigationBar(navController = navController)
        }
    ) { innerPadding ->
        // Pass activity and navigation setup to NavHostContainer
        NavHostContainer(
            navController = navController,
            activity = activity,
            modifier = Modifier.padding(innerPadding)
        )
    }
}
