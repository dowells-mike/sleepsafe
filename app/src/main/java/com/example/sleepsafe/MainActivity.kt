package com.example.sleepsafe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.rememberNavController
import com.example.sleepsafe.components.AccountSettingsIcon
import com.example.sleepsafe.ui.theme.SleepsafeTheme
import com.example.sleepsafe.components.BottomNavigationBar
import com.example.sleepsafe.components.NavHostContainer
import com.example.sleepsafe.screens.AuthScreen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class MainActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()

        setContent {
            SleepsafeTheme {
                MainScreen(auth)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(auth: FirebaseAuth) {
    val navController = rememberNavController()
    var currentUser by remember { mutableStateOf(auth.currentUser) }

    val updateCurrentUser: (FirebaseUser?) -> Unit = { user ->
        currentUser = user
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "SleepSafe", fontSize = 20.sp) },
                actions = {
                    if (currentUser != null) {
                        AccountSettingsIcon(navController, currentUser!!.photoUrl?.toString())
                    } else {
                        AccountSettingsIcon(navController)
                    }
                }
            )
        },
        bottomBar = {
            BottomNavigationBar(navController = navController)
        }
    ) { innerPadding ->
        if (currentUser == null) {
            AuthScreen(
                onGoogleSignIn = {
                    // Implement Google Sign-In logic here
                    // After successful sign-in, update `currentUser`
                },
                onEmailSignIn = { email, password ->
                    auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                updateCurrentUser(auth.currentUser)
                            } else {
                                // Handle login failure
                            }
                        }
                }
            )
        } else {
            NavHostContainer(navController = navController, modifier = Modifier.padding(innerPadding))
        }
    }
}
