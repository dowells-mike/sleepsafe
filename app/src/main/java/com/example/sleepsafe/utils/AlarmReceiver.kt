// AlarmReceiver.kt
package com.example.sleepsafe.utils

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.sleepsafe.R

/**
 * BroadcastReceiver to handle alarm events, including playing an alarm sound and showing notifications.
 */
class AlarmReceiver : BroadcastReceiver() {

    companion object {
        const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "alarm_channel"
        private var ringtone: Ringtone? = null

        /**
         * Stops the currently playing alarm sound.
         */
        fun stopAlarm() {
            try {
                ringtone?.stop()
                ringtone = null
                Log.d("AlarmReceiver", "Alarm sound stopped successfully")
            } catch (e: Exception) {
                Log.e("AlarmReceiver", "Error stopping alarm sound", e)
            }
        }
    }

    @SuppressLint("NotificationPermission")
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null) {
            Log.e("AlarmReceiver", "Received null context")
            return
        }

        try {
            when (intent?.action) {
                "com.example.sleepsafe.CANCEL_ALARM" -> handleAlarmCancellation()
                else -> handleAlarmTrigger(context)
            }
        } catch (e: Exception) {
            Log.e("AlarmReceiver", "Error handling alarm event", e)
        }
    }

    private fun handleAlarmCancellation() {
        Log.d("AlarmReceiver", "Canceling alarm")
        stopAlarm()
    }

    private fun handleAlarmTrigger(context: Context) {
        // Play alarm sound
        playAlarmSound(context)

        // Show notification
        showAlarmNotification(context)
    }

    private fun playAlarmSound(context: Context) {
        try {
            if (ringtone == null) {
                val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ringtone = RingtoneManager.getRingtone(context, alarmSound).apply {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        val audioAttributes = AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                        setAudioAttributes(audioAttributes)
                    }
                }
                ringtone?.play()
                Log.d("AlarmReceiver", "Alarm sound started playing")
            }
        } catch (e: Exception) {
            Log.e("AlarmReceiver", "Error playing alarm sound", e)
        }
    }

    private fun showAlarmNotification(context: Context) {
        try {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            createNotificationChannel(notificationManager)

            val notification = buildNotification(context)
            notificationManager.notify(NOTIFICATION_ID, notification)
            Log.d("AlarmReceiver", "Alarm notification shown successfully")
        } catch (e: Exception) {
            Log.e("AlarmReceiver", "Error showing notification", e)
        }
    }

    private fun createNotificationChannel(notificationManager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Alarm Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for alarms"
                setBypassDnd(true)
                setShowBadge(true)
                enableVibration(true)
                enableLights(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(context: Context): android.app.Notification {
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

        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_alarm)
            .setContentTitle("Wake Up!")
            .setContentText("Your alarm is ringing!")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .addAction(R.drawable.ic_snooze, "Snooze", snoozePendingIntent)
            .addAction(R.drawable.ic_stop, "Stop", stopPendingIntent)
            .setAutoCancel(false)
            .setOngoing(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .build()
    }
}
