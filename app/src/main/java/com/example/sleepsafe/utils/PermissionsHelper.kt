package com.example.sleepsafe.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

object PermissionsHelper {

    // List of permissions required by the app
    private val REQUIRED_PERMISSIONS = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(
            Manifest.permission.POST_NOTIFICATIONS,
            Manifest.permission.RECORD_AUDIO  // Keep other necessary permissions
        )
    } else {
        arrayOf(Manifest.permission.RECORD_AUDIO) // Pre-Android 13 doesn't need POST_NOTIFICATIONS
    }

    fun hasAllPermissions(context: Context): Boolean {
        return REQUIRED_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun requestPermissions(activity: ComponentActivity) {
        ActivityCompat.requestPermissions(
            activity,
            REQUIRED_PERMISSIONS,
            1001 // Request code for tracking permissions
        )
    }
}
