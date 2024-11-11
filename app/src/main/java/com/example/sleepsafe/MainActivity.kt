package com.example.sleepsafe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.sleepsafe.ui.theme.SleepsafeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SleepsafeTheme {
                HomeScreen()
            }
        }
    }
}

@Composable
//Homescreen activity
fun HomeScreen() {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Hours Slept: ", modifier = Modifier.padding(bottom = 8.dp))
        Text(text = "Sleep Quality Score: ", modifier = Modifier.padding(bottom = 16.dp))

        Button(onClick = {
            // Navigating to Analysis Page
            // todo: write the navigation logic
        }) {
            Text(text = "View Analysis")
        }

        Button(onClick = {
            // Navigating to settings
            // todo: write the navigation logic
        }, modifier = Modifier.padding(top = 8.dp)) {
            Text(text = "Settings")
        }
    }
}