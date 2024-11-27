// WelcomeScreen.kt
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.sleepsafe.utils.PermissionsHelper

/**
 * Displays the Welcome screen with an introduction to the app's features and a "Get Started" button.
 *
 * @param navController The NavController for navigating between screens.
 * @param activity The ComponentActivity for managing permissions and lifecycle.
 */
@Composable
fun WelcomeScreen(navController: NavController, activity: ComponentActivity) {
    // Pager state to manage the current page in the horizontal pager
    val pagerState = rememberPagerState(pageCount = { 3 }) // Number of pages in the pager

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Horizontal pager for swiping through feature pages
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f) // Take up remaining space
        ) { page ->
            when (page) {
                0 -> FeaturePage("Welcome to SleepSafe", "Track your sleep effortlessly!")
                1 -> FeaturePage("Sleep Analytics", "Understand your sleep patterns.")
                2 -> FeaturePage("Smart Alarms", "Wake up refreshed!")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                if (!PermissionsHelper.hasAllPermissions(activity)) {
                    PermissionsHelper.requestPermissions(activity)
                } else {
                    navController.navigate("home")
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Get Started")
        }
    }
}

/**
 * A composable function to display individual feature pages in the welcome screen.
 *
 * @param title The title of the feature.
 * @param description The description of the feature.
 */
@Composable
fun FeaturePage(title: String, description: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = title, style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = description, style = MaterialTheme.typography.bodyMedium)
    }
}
