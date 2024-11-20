package com.example.sleepsafe

// Importing necessary libraries for Android development and Jetpack Compose
import android.os.Bundle // For saving and restoring activity state
import androidx.activity.ComponentActivity // Base class for activities using Compose
import androidx.activity.compose.setContent // Function to set the Compose content of the activity
import androidx.compose.foundation.layout.padding // Modifier to add padding around composables
import androidx.compose.material3.ExperimentalMaterial3Api // Enables experimental Material 3 features
import androidx.compose.material3.Scaffold // Provides a structure for implementing Material Design layouts
import androidx.compose.material3.Text // Composable for displaying text
import androidx.compose.material3.TopAppBar // Composable for creating a top app bar
import androidx.compose.runtime.Composable // Annotation to indicate a function is a composable
import androidx.compose.ui.Modifier // Modifier to decorate or add behavior to composables
import androidx.compose.ui.unit.sp // Extension property for specifying text size in scaled pixels
import androidx.navigation.compose.rememberNavController // Function to create and remember a NavController

// Importing custom composables and theme
import com.example.sleepsafe.components.AccountSettingsIcon // Composable for an account settings icon (not used in provided code)
import com.example.sleepsafe.ui.theme.SleepsafeTheme // Custom theme for the application
import com.example.sleepsafe.components.BottomNavigationBar // Composable for the bottom navigation bar
import com.example.sleepsafe.components.NavHostContainer // Composable for the navigation host

// MainActivity updated to interact with ViewModels (though no ViewModel interaction is shown in this snippet)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { // Set the content of the activity using Jetpack Compose
            SleepsafeTheme { // Apply the custom SleepsafeTheme to the composables within
                MainScreen() // Call the MainScreen composable to build the UI
            }
        }
    }
}

// Enable experimental Material 3 APIs (required for TopAppBar in this example)
@OptIn(ExperimentalMaterial3Api::class)
// Composable function that defines the main screen of the application
@Composable
fun MainScreen() {
    // Create and remember a NavController for managing navigation within the app
    val navController = rememberNavController()

    // Use Scaffold to create a basic Material Design layout with top bar and bottom bar
    Scaffold(
        topBar = { // Define the top app bar
            TopAppBar(
                title = { Text(text = "SleepSafe", fontSize = 20.sp) } // Set the title of the top bar
            )
        },
        bottomBar = { // Define the bottom navigation bar
            BottomNavigationBar(navController = navController) // Pass the NavController to the BottomNavigationBar
        }
    ) { innerPadding -> // Lambda with padding values to be applied to the content
        // NavHostContainer is responsible for displaying the content based on the current navigation state
        NavHostContainer(navController = navController,
            modifier = Modifier.padding(innerPadding)) // Apply the padding provided by Scaffold
    }
}