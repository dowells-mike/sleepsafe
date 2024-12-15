// MainActivity.kt
package com.example.sleepsafe

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.example.sleepsafe.data.SleepDatabase
import com.example.sleepsafe.data.SleepData
import com.example.sleepsafe.ui.theme.SleepsafeTheme
import kotlinx.coroutines.launch
import com.example.sleepsafe.components.NavHostContainer
import com.example.sleepsafe.components.BottomNavigationBar

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SleepsafeTheme {
                // Use the main scaffold layout
                MainScaffold(this)
            }
        }

        // Test database insertion (Optional: You can keep this for testing)
        /*
        lifecycleScope.launch {
            val database = SleepDatabase.getDatabase(applicationContext)
            val sleepDao = database.sleepDao()

            // Insert sample data
            val sampleData = SleepData(timestamp = System.currentTimeMillis(), motion = 1.5f, audioLevel = 0.8f)
            sleepDao.insert(sampleData)

            // Query data
            val startTime = System.currentTimeMillis() - 24 * 60 * 60 * 1000 // 24 hours ago
            val endTime = System.currentTimeMillis()
            val data = sleepDao.getSleepDataBetween(startTime, endTime)

            Log.d("DatabaseTest", "Inserted and Retrieved Data: $data")
        }
         */
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