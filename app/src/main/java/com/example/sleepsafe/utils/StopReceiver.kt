package com.example.sleepsafe.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class StopReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("StopReceiver", "Alarm stopped")
        // Logic to stop the alarm completely
    }
}
