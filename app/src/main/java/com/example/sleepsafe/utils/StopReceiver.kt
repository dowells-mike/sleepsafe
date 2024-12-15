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
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null) return

        Log.d("StopReceiver", "Stopping alarm")

        // Stop the current alarm sound
        AlarmReceiver.stopAlarm()

        // Send broadcast to cancel the alarm
        val cancelAlarmIntent = Intent(context, AlarmReceiver::class.java).apply {
            action = "com.example.sleepsafe.CANCEL_ALARM"
        }
        context.sendBroadcast(cancelAlarmIntent)

        // Clear notifications related to the alarm
        NotificationManagerCompat.from(context).cancel(1001) // Cancel the alarm notification
    }
}
