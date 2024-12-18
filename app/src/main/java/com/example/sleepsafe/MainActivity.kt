// MainActivity.kt
package com.example.sleepsafe

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.example.sleepsafe.data.SleepDatabase
import com.example.sleepsafe.ui.theme.SleepsafeTheme
import com.example.sleepsafe.components.NavHostContainer
import com.example.sleepsafe.components.BottomNavigationBar
import com.example.sleepsafe.utils.PermissionsHelper
import kotlinx.coroutines.launch
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi

class MainActivity : ComponentActivity() {
    lateinit var permissionsHelper: PermissionsHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Register for permissions before creating PermissionsHelper
        val permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            // Check if we need to show the exact alarm permission dialog
            if (permissions.values.all { it } && permissionsHelper.needsExactAlarmPermission()) {
                permissionsHelper.openExactAlarmSettings()
            }
        }

        // Initialize PermissionsHelper with the launcher
        permissionsHelper = PermissionsHelper(this, permissionLauncher)

        // Set initial content
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            setInitialContent()
        }

        // Initialize database
        lifecycleScope.launch {
            try {
                SleepDatabase.getDatabase(applicationContext)
                Log.d("MainActivity", "Database initialized successfully")
            } catch (e: Exception) {
                Log.e("MainActivity", "Error initializing database", e)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onResume() {
        super.onResume()
        // Update content based on current permission state
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            setInitialContent()
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun setInitialContent() {
        val startDestination = if (!permissionsHelper.hasRequiredPermissions()) "welcome" else "home"

        setContent {
            SleepsafeTheme {
                if (startDestination == "home") {
                    MainScaffold(this)
                } else {
                    val navController = rememberNavController()
                    NavHostContainer(
                        navController = navController,
                        activity = this,
                        permissionsHelper = permissionsHelper,
                        modifier = Modifier,
                        startDestination = startDestination
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold(activity: ComponentActivity) {
    val navController = rememberNavController()

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
        NavHostContainer(
            navController = navController,
            activity = activity,
            permissionsHelper = (activity as MainActivity).permissionsHelper,
            modifier = Modifier.padding(innerPadding),
            startDestination = "home"
        )
    }
}
