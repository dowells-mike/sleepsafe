// SnoozeReceiver.kt
package com.example.sleepsafe.utils

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import java.util.*

/**
 * BroadcastReceiver to handle snooze functionality for alarms.
 */
class SnoozeReceiver : BroadcastReceiver() {
    companion object {
        private const val TAG = "SnoozeReceiver"
        private const val SNOOZE_MINUTES = 2 // Configurable snooze duration
    }

    @SuppressLint("ScheduleExactAlarm")
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null) {
            Log.e(TAG, "Received null context")
            return
        }

        try {
            Log.d(TAG, "Processing snooze request")

            // Stop the current alarm sound
            AlarmReceiver.stopAlarm()

            // Clear the current notification
            NotificationManagerCompat.from(context).cancel(AlarmReceiver.NOTIFICATION_ID)

            scheduleSnoozeAlarm(context)

            Log.d(TAG, "Alarm snoozed successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error snoozing alarm", e)
        }
    }

    private fun scheduleSnoozeAlarm(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Check for exact alarm permission on Android 12+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            Log.e(TAG, "Cannot schedule exact alarms - permission not granted")
            return
        }

        // Create a new alarm intent for snoozing
        val snoozeIntent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("isSnoozeAlarm", true)
        }

        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            System.currentTimeMillis().toInt(), // Use unique request code for each snooze
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Set the snooze time
        val snoozeTime = Calendar.getInstance().apply {
            add(Calendar.MINUTE, SNOOZE_MINUTES)
        }

        try {
            // Schedule the snoozed alarm
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                snoozeTime.timeInMillis,
                snoozePendingIntent
            )

            val formattedTime = java.text.SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                .format(snoozeTime.time)
            Log.d(TAG, "Snoozed alarm scheduled for: $formattedTime")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to schedule snooze alarm", e)
        }
    }
}
