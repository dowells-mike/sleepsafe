package com.example.sleepsafe.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationManagerCompat

class StopReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null) return

        Log.d("StopReceiver", "Stopping alarm")

        // Stop the current alarm sound
        AlarmReceiver.stopAlarm()

        // Cancel the alarm
        val cancelAlarmIntent = Intent(context, AlarmReceiver::class.java).apply {
            action = "com.example.sleepsafe.CANCEL_ALARM"
        }
        context.sendBroadcast(cancelAlarmIntent)

        // Clear notifications
        NotificationManagerCompat.from(context).cancel(1001) // Cancel the alarm notification
    }
}
