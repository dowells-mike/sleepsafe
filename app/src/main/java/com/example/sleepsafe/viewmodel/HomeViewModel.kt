// HomeViewModel.kt
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
import com.example.sleepsafe.data.SleepQualityMetrics
import com.example.sleepsafe.utils.SleepTracker
import com.example.sleepsafe.utils.SleepTrackingService
import com.example.sleepsafe.utils.AlarmReceiver
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * ViewModel for the Home screen, managing sleep time, alarms, and tracking status.
 */
class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val sleepDao = SleepDatabase.getDatabase(application).sleepDao()
    private val alarmManager = application.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    // Basic tracking state
    private val _sleepTime = MutableLiveData<Calendar?>()
    val sleepTime: LiveData<Calendar?> = _sleepTime

    private val _alarmTime = MutableLiveData<Long?>()
    val alarmTime: LiveData<Long?> = _alarmTime

    private val _isTracking = MutableLiveData(false)
    val isTracking: LiveData<Boolean> = _isTracking

    private val _permissionRequired = MutableLiveData(false)
    val permissionRequired: LiveData<Boolean> = _permissionRequired

    // Enhanced tracking state
    private val _currentSleepPhase = MutableLiveData<SleepTracker.SleepPhase>()
    val currentSleepPhase: LiveData<SleepTracker.SleepPhase> = _currentSleepPhase

    private val _currentSleepDuration = MutableLiveData<Long>()
    val currentSleepDuration: LiveData<Long> = _currentSleepDuration

    private val _lastSleepQuality = MutableLiveData<SleepQualityMetrics>()
    val lastSleepQuality: LiveData<SleepQualityMetrics> = _lastSleepQuality

    private var durationUpdateJob: Job? = null

    init {
        viewModelScope.launch {
            // Load last sleep session data
            try {
                val latestStart = sleepDao.getLatestSleepSessionStart()
                latestStart?.let { start ->
                    sleepDao.getSleepQualityMetrics(start)?.let { metrics ->
                        _lastSleepQuality.postValue(metrics)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading last sleep session", e)
            }
        }
    }

    /**
     * Sets the sleep time based on the selected hour and minute.
     */
    fun setSleepTime(hour: Int, minute: Int): Boolean {
        try {
            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
            }

            // If the selected time is in the past, move to next day
            if (calendar.before(Calendar.getInstance())) {
                calendar.add(Calendar.DATE, 1)
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
     * Sets the alarm based on the selected hour and minute.
     */
    @RequiresApi(Build.VERSION_CODES.S)
    fun setAlarm(hour: Int, minute: Int, useSmartAlarm: Boolean): Boolean {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !alarmManager.canScheduleExactAlarms()) {
                _permissionRequired.postValue(true)
                return false
            }

            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
            }

            if (calendar.before(Calendar.getInstance())) {
                calendar.add(Calendar.DATE, 1)
            }

            val alarmTimeInMillis = if (useSmartAlarm) {
                calculateSmartAlarmTime(calendar.timeInMillis)
            } else {
                calendar.timeInMillis
            }

            val intent = Intent(getApplication(), AlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                getApplication(),
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

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
     * Starts sleep tracking.
     */
    fun startTrackingNow(context: Context) {
        try {
            val sleepStartTime = _sleepTime.value?.timeInMillis ?: System.currentTimeMillis()
            val alarmTime = _alarmTime.value ?: 0L

            val intent = Intent(context, SleepTrackingService::class.java).apply {
                putExtra(SleepTrackingService.EXTRA_SLEEP_START, sleepStartTime)
                putExtra(SleepTrackingService.EXTRA_ALARM_TIME, alarmTime)
            }

            SleepTrackingService.startService(context, intent)
            _isTracking.postValue(true)
            startDurationUpdates(sleepStartTime)

            viewModelScope.launch {
                try {
                    val initialData = SleepData(
                        timestamp = System.currentTimeMillis(),
                        motion = 0f,
                        audioLevel = 0f,
                        sleepStart = sleepStartTime,
                        alarmTime = alarmTime,
                        sleepPhase = SleepTracker.SleepPhase.AWAKE.name
                    )
                    sleepDao.insert(initialData)
                    Log.d(TAG, "Initial sleep data inserted")
                } catch (e: Exception) {
                    Log.e(TAG, "Error inserting initial sleep data", e)
                }
            }

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
            durationUpdateJob?.cancel()

            // Update last sleep quality
            viewModelScope.launch {
                try {
                    val latestStart = sleepDao.getLatestSleepSessionStart()
                    latestStart?.let { start ->
                        sleepDao.getSleepQualityMetrics(start)?.let { metrics ->
                            _lastSleepQuality.postValue(metrics)
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error updating sleep quality", e)
                }
            }

            Log.d(TAG, "Sleep tracking stopped")
            showToast("Sleep tracking stopped")
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping tracking", e)
            showToast("Failed to stop sleep tracking")
        }
    }

    private fun startDurationUpdates(startTime: Long) {
        durationUpdateJob?.cancel()
        durationUpdateJob = viewModelScope.launch {
            while (true) {
                val duration = System.currentTimeMillis() - startTime
                _currentSleepDuration.postValue(duration)
                delay(1000) // Update every second
            }
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
     * Gets the exact alarm permission intent.
     */
    fun getExactAlarmPermissionIntent(): Intent? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
        } else {
            null
        }
    }

    private fun formatTime(calendar: Calendar): String {
        return SimpleDateFormat("HH:mm", Locale.getDefault()).format(calendar.time)
    }

    private fun showToast(message: String) {
        Toast.makeText(getApplication(), message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val TAG = "HomeViewModel"
    }
}
