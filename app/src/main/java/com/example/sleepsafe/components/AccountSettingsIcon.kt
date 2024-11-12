package com.example.sleepsafe.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle

@Composable
fun AccountSettingsIcon(navController: NavHostController, avatarUrl: String? = null) {
    IconButton(onClick = {
        navController.navigate("account")
    }) {
        if (avatarUrl != null) {
            AsyncImage(
                model = avatarUrl,
                contentDescription = "User Avatar",
                modifier = Modifier.size(32.dp),
                contentScale = ContentScale.Crop
            )
        } else {
            Icon(imageVector = Icons.Default.AccountCircle, contentDescription = "Account Settings", tint = Color.Gray)
        }
    }
}
