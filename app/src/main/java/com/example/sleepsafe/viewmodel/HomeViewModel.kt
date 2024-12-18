package com.example.sleepsafe.viewmodel

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.sleepsafe.data.SleepDatabase
import com.example.sleepsafe.data.SleepData
import com.example.sleepsafe.utils.SleepTrackingService
import com.example.sleepsafe.utils.AlarmReceiver
import kotlinx.coroutines.launch
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

    companion object {
        private const val TAG = "HomeViewModel"
        private val TIME_FORMAT = SimpleDateFormat("hh:mm a", Locale.getDefault())
    }

    /**
     * Sets the sleep time based on the selected hour and minute.
     * @return Boolean indicating if the operation was successful
     */
    fun setSleepTime(hour: Int, minute: Int): Boolean {
        try {
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
            Log.d(TAG, "Sleep time set: ${formatTime(calendar)}")
            showToast("Sleep time set for ${formatTime(calendar)}")
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Error setting sleep time", e)
            showToast("Failed to set sleep time")
            return false
        }
    }

    /**
     * Clears the currently set sleep time.
     */
    fun clearSleepTime() {
        try {
            _sleepTime.postValue(null)
            Log.d(TAG, "Sleep time cleared")
            showToast("Sleep time cleared")
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing sleep time", e)
            showToast("Failed to clear sleep time")
        }
    }

    /**
     * Sets the alarm based on the selected hour and minute.
     * Handles smart alarm logic if enabled.
     * @return Boolean indicating if the alarm was successfully set
     */
    @RequiresApi(Build.VERSION_CODES.S)
    fun setAlarm(hour: Int, minute: Int, useSmartAlarm: Boolean): Boolean {
        try {
            // Check permission first
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !alarmManager.canScheduleExactAlarms()) {
                _permissionRequired.postValue(true)
                return false
            }

            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
            }

            // If the alarm time is in the past, set it for the next day
            if (calendar.before(Calendar.getInstance())) {
                calendar.add(Calendar.DATE, 1)
            }

            val alarmTimeInMillis = if (useSmartAlarm) {
                calculateSmartAlarmTime(calendar.timeInMillis)
            } else {
                calendar.timeInMillis
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
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                alarmTimeInMillis,
                pendingIntent
            )

            _alarmTime.postValue(alarmTimeInMillis)
            val timeStr = formatTime(Calendar.getInstance().apply { timeInMillis = alarmTimeInMillis })
            Log.d(TAG, "Alarm set: $timeStr")
            showToast("Alarm set for $timeStr")
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Error setting alarm", e)
            showToast("Failed to set alarm")
            return false
        }
    }

    /**
     * Calculates the optimal wake time for smart alarm.
     */
    private fun calculateSmartAlarmTime(targetTime: Long): Long {
        val windowStart = targetTime - (30 * 60 * 1000) // 30 minutes before
        // TODO: Implement sleep cycle analysis logic
        return targetTime
    }

    /**
     * Cancels the currently set alarm.
     */
    fun cancelAlarm() {
        try {
            val intent = Intent(getApplication(), AlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                getApplication(),
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            alarmManager.cancel(pendingIntent)
            _alarmTime.postValue(null)
            Log.d(TAG, "Alarm canceled")
            showToast("Alarm canceled")
        } catch (e: Exception) {
            Log.e(TAG, "Error canceling alarm", e)
            showToast("Failed to cancel alarm")
        }
    }

    /**
     * Starts sleep tracking now and schedules future tracking based on sleep time if set.
     */
    fun startTrackingNow(context: Context) {
        try {
            val sleepStartTime = _sleepTime.value?.timeInMillis ?: System.currentTimeMillis()
            val alarmTime = _alarmTime.value ?: 0L

            // Create intent with required extras
            val intent = Intent(context, SleepTrackingService::class.java).apply {
                putExtra(SleepTrackingService.EXTRA_SLEEP_START, sleepStartTime)
                putExtra(SleepTrackingService.EXTRA_ALARM_TIME, alarmTime)
            }

            // Start the service
            SleepTrackingService.startService(context, intent)
            _isTracking.postValue(true)

            // Insert initial sleep data
            viewModelScope.launch {
                try {
                    val database = SleepDatabase.getDatabase(context)
                    val sleepDao = database.sleepDao()
                    val initialData = SleepData(
                        timestamp = System.currentTimeMillis(),
                        motion = 0f,
                        audioLevel = 0f,
                        sleepStart = sleepStartTime,
                        alarmTime = alarmTime
                    )
                    sleepDao.insert(initialData)
                    Log.d(TAG, "Initial sleep data inserted: $initialData")
                } catch (e: Exception) {
                    Log.e(TAG, "Error inserting initial sleep data", e)
                }
            }

            Log.d(TAG, "Tracking started with Sleep Start: $sleepStartTime, Alarm Time: $alarmTime")
            showToast("Sleep tracking started")
        } catch (e: Exception) {
            Log.e(TAG, "Error starting tracking", e)
            showToast("Failed to start sleep tracking")
            _isTracking.postValue(false)
        }
    }

    /**
     * Stops sleep tracking.
     */
    fun stopTracking(context: Context) {
        try {
            SleepTrackingService.stopService(context)
            _isTracking.postValue(false)
            Log.d(TAG, "Sleep tracking stopped")
            showToast("Sleep tracking stopped")
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping tracking", e)
            showToast("Failed to stop sleep tracking")
        }
    }

    /**
     * Formats a Calendar object into a human-readable time string.
     */
    fun formatTime(calendar: Calendar): String {
        return TIME_FORMAT.format(calendar.time)
    }

    /**
     * Shows a toast message.
     */
    private fun showToast(message: String) {
        Toast.makeText(getApplication(), message, Toast.LENGTH_SHORT).show()
    }

    /**
     * Requests the permission to schedule exact alarms.
     * @return Intent that needs to be started from an activity
     */
    fun getExactAlarmPermissionIntent(): Intent? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
        } else {
            null
        }
    }
}
