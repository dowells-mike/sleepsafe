package com.example.sleepsafe

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import com.example.sleepsafe.components.BottomNavigationBar
import com.example.sleepsafe.components.NavHostContainer
import com.example.sleepsafe.screens.AuthScreen
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(auth: FirebaseAuth, googleSignInClient: GoogleSignInClient) {
    val navController = rememberNavController()
    var currentUser by remember { mutableStateOf(auth.currentUser) }

    val updateCurrentUser: (FirebaseUser?) -> Unit = { user ->
        currentUser = user
    }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        handleSignInResult(task, auth, updateCurrentUser)
    }

    if (currentUser == null) {
        AuthScreen(
            onGoogleSignIn = {
                val signInIntent = googleSignInClient.signInIntent
                launcher.launch(signInIntent)
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
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(text = "SleepSafe", fontSize = 20.sp) },
                    actions = {
                        AccountSettingsIcon(navController, currentUser?.photoUrl?.toString())
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
}
