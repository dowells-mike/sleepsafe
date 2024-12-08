// AlarmReceiver.kt
package com.example.sleepsafe.utils

import android.annotation.SuppressLint
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.sleepsafe.R
import com.example.sleepsafe.viewmodel.HomeViewModel

/**
 * BroadcastReceiver to handle alarm events, including playing an alarm sound and showing notifications.
 */
class AlarmReceiver : BroadcastReceiver() {

    companion object {
        private var ringtone: Ringtone? = null

        /**
         * Stops the currently playing alarm sound.
         */
        fun stopAlarm() {
            ringtone?.stop()
            ringtone = null
        }
    }

    @SuppressLint("NotificationPermission")
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null) return

        // Handle alarm cancellation
        if (intent?.action == "com.example.sleepsafe.CANCEL_ALARM") {
            Log.d("AlarmReceiver", "Canceling alarm from StopReceiver")
            val homeViewModel = HomeViewModel(context.applicationContext as Application)
            homeViewModel.cancelAlarm()
            return
        }

        // Play the alarm sound
        if (ringtone == null) {
            val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ringtone = RingtoneManager.getRingtone(context, alarmSound)
            ringtone?.play()
        }

        // Create a notification
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create notification channel (required for Android 8+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "alarm_channel",
                "Alarm Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for alarms"
                setBypassDnd(true) // Allow overriding Do Not Disturb
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Snooze action
        val snoozeIntent = Intent(context, SnoozeReceiver::class.java)
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Stop action
        val stopIntent = Intent(context, StopReceiver::class.java)
        val stopPendingIntent = PendingIntent.getBroadcast(
            context,
            1,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Build the notification
        val notification = NotificationCompat.Builder(context, "alarm_channel")
            .setSmallIcon(R.drawable.ic_alarm) // Replace with your app's alarm icon
            .setContentTitle("Wake Up!")
            .setContentText("Your alarm is ringing!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .addAction(R.drawable.ic_snooze, "Snooze", snoozePendingIntent)
            .addAction(R.drawable.ic_stop, "Stop", stopPendingIntent)
            .setDefaults(NotificationCompat.DEFAULT_ALL) // Adds sound and vibration
            .build()

        Log.d("AlarmReceiver", "Notification built and being shown.")
        notificationManager.notify(1001, notification)
    }
}
