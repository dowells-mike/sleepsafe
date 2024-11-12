package com.example.sleepsafe.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AuthScreen(onGoogleSignIn: () -> Unit, onEmailSignIn: (email: String, password: String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Welcome to SleepSafe", fontSize = 24.sp)
        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { onGoogleSignIn() }) {
            Text("Sign in with Google")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            // Implement your email and password login logic here.
            onEmailSignIn("test@example.com", "password")
        }) {
            Text("Sign in with Email")
        }
    }
}
