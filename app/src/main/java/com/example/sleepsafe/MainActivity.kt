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
import com.example.sleepsafe.components.AccountSettingsIcon
import com.example.sleepsafe.ui.theme.SleepsafeTheme
import com.example.sleepsafe.components.BottomNavigationBar
import com.example.sleepsafe.components.NavHostContainer

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SleepsafeTheme {
                MainScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "SleepSafe", fontSize = 20.sp) },
                actions = {
                    AccountSettingsIcon(navController)  // Pass the navController here
                }
            )
        },
        bottomBar = {
            BottomNavigationBar(navController = navController)
        }
    ) { innerPadding ->
        NavHostContainer(navController = navController, modifier = Modifier.padding(innerPadding))
    }
}
