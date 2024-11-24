package com.example.sleepsafe.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class SnoozeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("SnoozeReceiver", "Alarm snoozed for 2 minutes")
        // Logic for snoozing the alarm for 2 minutes
    }
}
