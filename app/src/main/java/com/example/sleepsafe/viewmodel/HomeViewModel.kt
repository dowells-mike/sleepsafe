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
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val sleepDao = SleepDatabase.getDatabase(application).sleepDao()
    private val alarmManager = application.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    private val _sleepTime = MutableLiveData<Calendar?>()
    val sleepTime: LiveData<Calendar?> = _sleepTime

    private val _alarmTime = MutableLiveData<Long?>()
    val alarmTime: LiveData<Long?> = _alarmTime

    private val _isTracking = MutableLiveData(false)
    val isTracking: LiveData<Boolean> = _isTracking

    private val _permissionRequired = MutableLiveData(false)
    val permissionRequired: LiveData<Boolean> = _permissionRequired

    private val _currentSleepPhase = MutableLiveData<SleepTracker.SleepPhase>(SleepTracker.SleepPhase.AWAKE)
    val currentSleepPhase: LiveData<SleepTracker.SleepPhase> = _currentSleepPhase

    private val _currentSleepDuration = MutableLiveData<Long>()
    val currentSleepDuration: LiveData<Long> = _currentSleepDuration

    private val _lastSleepQuality = MutableLiveData<SleepQualityMetrics>()
    val lastSleepQuality: LiveData<SleepQualityMetrics> = _lastSleepQuality

    private var durationUpdateJob: Job? = null
    private var phaseUpdateJob: Job? = null

    init {
        loadLastSleepSession()
    }

    private fun loadLastSleepSession() {
        viewModelScope.launch {
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

    fun setSleepTime(calendar: Calendar) {
        _sleepTime.postValue(calendar)
        Log.d(TAG, "Sleep time set: ${formatTime(calendar)}")
        showToast("Bedtime set for ${formatTime(calendar)}")
    }

    @RequiresApi(Build.VERSION_CODES.S)
    fun setAlarm(timestamp: Long) {
        try {
            if (!checkAlarmPermission()) {
                return
            }

            val alarmIntent = Intent(getApplication(), AlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                getApplication(),
                0,
                alarmIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                timestamp,
                pendingIntent
            )

            _alarmTime.postValue(timestamp)
            val timeStr = formatTime(Calendar.getInstance().apply { timeInMillis = timestamp })
            Log.d(TAG, "Alarm set: $timeStr")
            showToast("Alarm set for $timeStr")
        } catch (e: Exception) {
            Log.e(TAG, "Error setting alarm", e)
            showToast("Failed to set alarm")
        }
    }

    fun startTracking(context: Context) {
        try {
            val startTime = _sleepTime.value?.timeInMillis ?: System.currentTimeMillis()
            val endTime = _alarmTime.value ?: (startTime + (8 * 60 * 60 * 1000))

            startTrackingInternal(context, startTime, endTime)
        } catch (e: Exception) {
            Log.e(TAG, "Error starting tracking", e)
            showToast("Failed to start sleep tracking")
            _isTracking.postValue(false)
        }
    }

    fun startQuickTest(context: Context, durationMinutes: Int) {
        try {
            val startTime = System.currentTimeMillis()
            val endTime = startTime + (durationMinutes * 60 * 1000)

            val currentSleepTime = _sleepTime.value
            val currentAlarmTime = _alarmTime.value

            startTrackingInternal(context, startTime, endTime)

            _sleepTime.value = currentSleepTime
            _alarmTime.value = currentAlarmTime
        } catch (e: Exception) {
            Log.e(TAG, "Error starting quick test", e)
            showToast("Failed to start sleep tracking")
            _isTracking.postValue(false)
        }
    }

    @SuppressLint("NewApi")
    private fun startTrackingInternal(context: Context, startTime: Long, endTime: Long) {
        val intent = Intent(context, SleepTrackingService::class.java).apply {
            putExtra(SleepTrackingService.EXTRA_SLEEP_START, startTime)
            putExtra(SleepTrackingService.EXTRA_ALARM_TIME, endTime)
        }

        SleepTrackingService.startService(context, intent)
        _isTracking.postValue(true)
        startDurationUpdates(startTime)
        startPhaseUpdates()

        setAlarm(endTime)

        viewModelScope.launch {
            try {
                val initialData = SleepData(
                    timestamp = startTime,
                    motion = 0f,
                    audioLevel = 0f,
                    sleepStart = startTime,
                    alarmTime = endTime,
                    sleepPhase = SleepTracker.SleepPhase.AWAKE.name
                )
                sleepDao.insert(initialData)
                Log.d(TAG, "Initial sleep data inserted")
            } catch (e: Exception) {
                Log.e(TAG, "Error inserting initial sleep data", e)
            }
        }

        showToast("Sleep tracking started")
    }

    fun stopTracking(context: Context) {
        try {
            SleepTrackingService.stopService(context)
            _isTracking.postValue(false)
            durationUpdateJob?.cancel()
            phaseUpdateJob?.cancel()
            loadLastSleepSession()
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
                delay(1000)
            }
        }
    }

    private fun startPhaseUpdates() {
        phaseUpdateJob?.cancel()
        phaseUpdateJob = viewModelScope.launch {
            while (true) {
                try {
                    val latestData = sleepDao.getLatestSleepData()
                    latestData?.let { data ->
                        val phase = SleepTracker.SleepPhase.valueOf(data.sleepPhase)
                        _currentSleepPhase.postValue(phase)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error updating sleep phase", e)
                }
                delay(5000) // Update every 5 seconds
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun checkAlarmPermission(): Boolean {
        if (!alarmManager.canScheduleExactAlarms()) {
            _permissionRequired.postValue(true)
            return false
        }
        return true
    }

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

    override fun onCleared() {
        super.onCleared()
        durationUpdateJob?.cancel()
        phaseUpdateJob?.cancel()
    }
}
