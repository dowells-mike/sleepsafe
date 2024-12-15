// SnoozeReceiver.kt
package com.example.sleepsafe.utils

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.sleepsafe.utils.AlarmReceiver
import java.util.*

/**
 * BroadcastReceiver to handle snooze functionality for alarms.
 * Schedules a new alarm 2 minutes after the snooze action is triggered.
 */
class SnoozeReceiver : BroadcastReceiver() {
    @SuppressLint("ScheduleExactAlarm")
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null) return

        Log.d("SnoozeReceiver", "Snoozing alarm for 2 minutes")

        // Stop the current alarm sound
        AlarmReceiver.stopAlarm()

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Create a new alarm intent for snoozing
        val snoozeIntent = Intent(context, AlarmReceiver::class.java)
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context, 0, snoozeIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Set the snooze time (2 minutes from now)
        val snoozeTime = Calendar.getInstance().apply {
            add(Calendar.MINUTE, 2)
        }

        // Schedule the snoozed alarm
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            snoozeTime.timeInMillis,
            snoozePendingIntent
        )

        Log.d("SnoozeReceiver", "Snoozed alarm scheduled for: ${snoozeTime.time}")
    }
}
