// StopReceiver.kt
package com.example.sleepsafe.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import com.example.sleepsafe.utils.AlarmReceiver

/**
 * BroadcastReceiver to handle stopping the alarm and clearing related notifications.
 */
class StopReceiver : BroadcastReceiver() {
    companion object {
        private const val TAG = "StopReceiver"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null) {
            Log.e(TAG, "Received null context")
            return
        }

        try {
            Log.d(TAG, "Processing stop alarm request")

            // Stop the current alarm sound
            AlarmReceiver.stopAlarm()

            // Send broadcast to cancel the alarm
            val cancelAlarmIntent = Intent(context, AlarmReceiver::class.java).apply {
                action = "com.example.sleepsafe.CANCEL_ALARM"
            }
            context.sendBroadcast(cancelAlarmIntent)

            // Clear notifications related to the alarm
            NotificationManagerCompat.from(context).apply {
                cancel(AlarmReceiver.NOTIFICATION_ID)
                Log.d(TAG, "Alarm notification cleared")
            }

            Log.d(TAG, "Alarm stopped successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping alarm", e)
        }
    }
}
