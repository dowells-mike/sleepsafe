// TimePickerDialog.kt
package com.example.sleepsafe.screens

import android.app.TimePickerDialog
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext

/**
 * A composable function to display a native Android TimePickerDialog for selecting a time.
 *
 * @param initialHour The initial hour displayed in the dialog.
 * @param initialMinute The initial minute displayed in the dialog.
 * @param onTimeSelected Callback invoked when the user selects a time.
 * @param onDismiss Callback invoked when the dialog is dismissed without selecting a time.
 */
@Composable
fun TimePickerDialog(
    initialHour: Int,
    initialMinute: Int,
    onTimeSelected: (hour: Int, minute: Int) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    DisposableEffect(Unit) {
        val timePickerDialog = TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                onTimeSelected(hourOfDay, minute)
            },
            initialHour,
            initialMinute,
            false // Indicates a 12-hour clock format
        )

        // Handle dismissal of the dialog
        timePickerDialog.setOnCancelListener { onDismiss() }
        timePickerDialog.show()

        onDispose {
            timePickerDialog.dismiss()
        }
    }
}
