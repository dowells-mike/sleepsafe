package com.example.sleepsafe

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.sleepsafe.utils.AlarmReceiver
import java.util.Calendar

class AlarmActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SnoozeStopDialog(onSnooze = { snoozeAlarm() }, onStop = { stopAlarm() })
        }
    }

    @SuppressLint("ScheduleExactAlarm")
    private fun snoozeAlarm() {
        AlarmReceiver.stopAlarm()
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val snoozeIntent = Intent(this, AlarmReceiver::class.java)
        val snoozePendingIntent = PendingIntent.getBroadcast(
            this, 0, snoozeIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val snoozeTime = Calendar.getInstance().apply { add(Calendar.MINUTE, 2) }
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            snoozeTime.timeInMillis,
            snoozePendingIntent
        )

        finish()
    }

    private fun stopAlarm() {
        AlarmReceiver.stopAlarm()
        finish()
    }
}

@Composable
fun SnoozeStopDialog(onSnooze: () -> Unit, onStop: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Button(onClick = onSnooze) {
                Text("Snooze")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onStop) {
                Text("Stop")
            }
        }
    }
}
