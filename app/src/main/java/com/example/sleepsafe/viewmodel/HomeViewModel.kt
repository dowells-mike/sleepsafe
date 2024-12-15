// HomeViewModel.kt
package com.example.sleepsafe.viewmodel

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.sleepsafe.utils.AlarmReceiver
import java.text.SimpleDateFormat
import java.util.*

/**
 * ViewModel for the Home screen, managing sleep time, alarms, and tracking status.
 */
class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val _sleepTime = MutableLiveData<Calendar?>()
    val sleepTime: LiveData<Calendar?> get() = _sleepTime

    private val _alarmTime = MutableLiveData<Long?>()
    val alarmTime: LiveData<Long?> get() = _alarmTime

    private val _isTracking = MutableLiveData(false)
    val isTracking: LiveData<Boolean> get() = _isTracking

    private val _permissionRequired = MutableLiveData(false)
    val permissionRequired: LiveData<Boolean> = _permissionRequired

    private val alarmManager = application.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    /**
     * Sets the sleep time based on the selected hour and minute.
     */
    fun setSleepTime(hour: Int, minute: Int) {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
        }

        // Check if the selected time is in the past and adjust accordingly
        if (calendar.before(Calendar.getInstance())) {
            calendar.add(Calendar.DATE, 1) // Move to the next day
        }

        _sleepTime.postValue(calendar)
        Log.d("HomeViewModel", "Sleep time set: ${formatTime(calendar)}")
    }

    /**
     * Clears the currently set sleep time.
     */
    fun clearSleepTime() {
        _sleepTime.postValue(null)
        Log.d("HomeViewModel", "Sleep time cleared")
    }

    /**
     * Sets the alarm based on the selected hour and minute.
     * Handles smart alarm logic if enabled.
     */
    @SuppressLint("NewApi")
    fun setAlarm(hour: Int, minute: Int, useSmartAlarm: Boolean) {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
        }

        // If the alarm time is in the past, set it for the next day
        if (calendar.before(Calendar.getInstance())) {
            calendar.add(Calendar.DATE, 1)
        }

        // Smart alarm handling (optional)
        val alarmTimeInMillis = if (useSmartAlarm) {
            val smartAlarmWindowStart = calendar.timeInMillis - (30 * 60 * 1000) // 30 minutes before
            val smartAlarmWindowEnd = calendar.timeInMillis
            // Placeholder: Implement logic to determine the best alarm time within the window
            // based on sleep data analysis (e.g., lightest sleep phase).
            // For now, we'll just use the original alarm time.
            calendar.timeInMillis
        } else {
            calendar.timeInMillis
        }

        // Check if the device is on Doze mode
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !alarmManager.canScheduleExactAlarms()) {
            _permissionRequired.postValue(true)
            return // Cannot set exact alarm without permission
        }

        // Create an intent for the AlarmReceiver
        val intent = Intent(getApplication(), AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            getApplication(),
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Set the alarm
        // Use setExactAndAllowWhileIdle to trigger the alarm even in Doze mode
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            alarmTimeInMillis,
            pendingIntent
        )

        _alarmTime.postValue(alarmTimeInMillis)
        Log.d("HomeViewModel", "Alarm set: ${formatTime(Calendar.getInstance().apply { timeInMillis = alarmTimeInMillis })}")
    }

    /**
     * Cancels the currently set alarm.
     */
    fun cancelAlarm() {
        val intent = Intent(getApplication(), AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            getApplication(),
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        alarmManager.cancel(pendingIntent)
        _alarmTime.postValue(null)
        Log.d("HomeViewModel", "Alarm canceled")
    }

    /**
     * Starts sleep tracking now and schedules future tracking based on sleep time if set.
     */
    fun startTrackingNow(context: Context) {
        _isTracking.postValue(true)
        Log.d("HomeViewModel", "Sleep tracking started")
        // Additional logic to start data collection, etc.
    }

    /**
     * Stops sleep tracking.
     */
    fun stopTracking(context: Context) {
        _isTracking.postValue(false)
        Log.d("HomeViewModel", "Sleep tracking stopped")
        // Additional logic to stop data collection, etc.
    }

    /**
     * Formats a Calendar object into a human-readable time string.
     */
    private fun formatTime(calendar: Calendar): String {
        return SimpleDateFormat("hh:mm a", Locale.getDefault()).format(calendar.time)
    }

    /**
     * Requests the permission to schedule exact alarms.
     */
    fun requestExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
            // You need to start this intent from an Activity context
            // Consider using a callback to the Activity or a different approach to launch this intent
            // intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK // Required for startActivity outside of an Activity context
            // getApplication<Application>().startActivity(intent)
        }
    }
}