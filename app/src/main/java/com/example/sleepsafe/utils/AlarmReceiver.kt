package com.example.sleepsafe.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val useSmartAlarm = intent?.getBooleanExtra("useSmartAlarm", false) ?: false
        val alarmTime = intent?.getLongExtra("alarmTime", 0L) ?: 0L

        Log.d("AlarmReceiver", "Alarm triggered. Smart Alarm: $useSmartAlarm, Time: $alarmTime")
        Toast.makeText(context, "Alarm Ringing!", Toast.LENGTH_LONG).show()

        // Trigger notification or wake-up activity here
    }
}
