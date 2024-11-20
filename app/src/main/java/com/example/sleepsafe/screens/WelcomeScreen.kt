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

@Composable
fun WelcomeScreen(navController: NavController, activity: ComponentActivity) {
    // Pager state to manage current page
    val pagerState = rememberPagerState(pageCount = { 3 }) // Number of pages in the pager

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Pager composable to swipe between pages
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

