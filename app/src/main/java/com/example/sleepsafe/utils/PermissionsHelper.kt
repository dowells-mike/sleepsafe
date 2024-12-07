// PermissionsHelper.kt
package com.example.sleepsafe.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

/**
 * Utility object for managing app permissions.
 */
object PermissionsHelper {

    // List of required permissions
    private val REQUIRED_PERMISSIONS = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(
            Manifest.permission.POST_NOTIFICATIONS,
            Manifest.permission.RECORD_AUDIO
        )
    } else {
        arrayOf(Manifest.permission.RECORD_AUDIO)
    }

    /**
     * Checks if all required permissions are granted.
     *
     * @param context The application context.
     * @return True if all permissions are granted, false otherwise.
     */
    fun hasAllPermissions(context: Context): Boolean {
        return REQUIRED_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    /**
     * Requests the required permissions from the user.
     *
     * @param activity The ComponentActivity for requesting permissions.
     */
    fun requestPermissions(activity: ComponentActivity) {
        ActivityCompat.requestPermissions(
            activity,
            REQUIRED_PERMISSIONS,
            1001 // Request code for permissions
        )
    }
}
