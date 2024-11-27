// HomeViewModel.kt
package com.example.sleepsafe.viewmodel

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.sleepsafe.data.SleepData
import com.example.sleepsafe.data.SleepDatabase
import com.example.sleepsafe.utils.AlarmReceiver
import com.example.sleepsafe.utils.AudioRecorder
import com.example.sleepsafe.utils.SleepTrackingService
import kotlinx.coroutines.launch
import java.util.*
import kotlin.math.sqrt

/**
 * ViewModel to handle the logic for the Home screen.
 *
 * @param application The application context for managing system services and resources.
 */
@SuppressLint("StaticFieldLeak")
class HomeViewModel(application: Application) : AndroidViewModel(application), SensorEventListener {

    private val sensorManager: SensorManager =
        application.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private val context: Context = application.applicationContext
    private val sleepDao = SleepDatabase.getDatabase(application).sleepDao()
    private val audioRecorder = AudioRecorder(application)

    // LiveData for UI state management
    private val _motionState = MutableLiveData<String>()
    val motionState: LiveData<String> get() = _motionState

    private val _isRecording = MutableLiveData<Boolean>()
    val isRecording: LiveData<Boolean> get() = _isRecording

    private val _audioFilePath = MutableLiveData<String?>()
    val audioFilePath: LiveData<String?> get() = _audioFilePath

    private val _alarmTime = MutableLiveData<Long?>()
    val alarmTime: LiveData<Long?> get() = _alarmTime

    private val _sleepTime = MutableLiveData<Calendar?>()
    val sleepTime: LiveData<Calendar?> get() = _sleepTime

    private val _isTracking = MutableLiveData<Boolean>(false)
    val isTracking: LiveData<Boolean> get() = _isTracking

    private val _permissionRequired = MutableLiveData<Boolean>()
    val permissionRequired: LiveData<Boolean> get() = _permissionRequired

    private val gravity = FloatArray(3) { 0f }
    private val linearAcceleration = FloatArray(3) { 0f }

    init {
        startAccelerometerTracking()
    }

    /**
     * Starts accelerometer tracking to detect motion.
     */
    private fun startAccelerometerTracking() {
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    /**
     * Stops accelerometer tracking.
     */
    private fun stopAccelerometerTracking() {
        sensorManager.unregisterListener(this)
    }

    /**
     * Starts audio recording and updates the LiveData for the recording state and file path.
     */
    fun startAudioRecording() {
        try {
            _isRecording.postValue(true)
            val filePath = audioRecorder.startRecording()
            _audioFilePath.postValue(filePath)
        } catch (e: Exception) {
            _isRecording.postValue(false)
            e.printStackTrace()
        }
    }

    /**
     * Stops audio recording and updates the LiveData for the recording state.
     */
    fun stopAudioRecording() {
        _isRecording.postValue(false)
        audioRecorder.stopRecording()
    }

    /**
     * Sets an alarm with the specified hour, minute, and smart alarm option.
     *
     * @param hour The hour of the alarm.
     * @param minute The minute of the alarm.
     * @param useSmartAlarm Whether to use smart alarm features.
     */
    fun setAlarm(hour: Int, minute: Int, useSmartAlarm: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !hasExactAlarmPermission()) {
            _permissionRequired.postValue(true)
            return
        }

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            if (before(Calendar.getInstance())) add(Calendar.DAY_OF_YEAR, 1)
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("useSmartAlarm", useSmartAlarm)
            putExtra("alarmTime", calendar.timeInMillis)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)

        context.getSharedPreferences("SleepSafePrefs", Context.MODE_PRIVATE)
            .edit().putLong("alarmTime", calendar.timeInMillis).apply()

        _alarmTime.postValue(calendar.timeInMillis)
        Log.d("HomeViewModel", "Alarm set for: ${calendar.time}")
    }

    /**
     * Cancels the currently set alarm.
     */
    fun cancelAlarm() {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
        _alarmTime.postValue(null)
        Log.d("HomeViewModel", "Alarm canceled")
    }

    /**
     * Starts sleep tracking immediately.
     *
     * @param context The context to start the foreground service.
     */
    fun startTrackingNow(context: Context) {
        _isTracking.postValue(true)
        SleepTrackingService.startService(context)
    }

    /**
     * Stops sleep tracking.
     *
     * @param context The context to stop the foreground service.
     */
    fun stopTracking(context: Context) {
        _isTracking.postValue(false)
        SleepTrackingService.stopService(context)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || event.sensor.type != Sensor.TYPE_ACCELEROMETER) return

        val alpha = 0.8f
        gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0]
        gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1]
        gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2]

        linearAcceleration[0] = event.values[0] - gravity[0]
        linearAcceleration[1] = event.values[1] - gravity[1]
        linearAcceleration[2] = event.values[2] - gravity[2]

        val magnitude = sqrt(
            linearAcceleration[0] * linearAcceleration[0] +
                    linearAcceleration[1] * linearAcceleration[1] +
                    linearAcceleration[2] * linearAcceleration[2]
        )

        val movementThreshold = 1.5f
        _motionState.postValue(if (magnitude > movementThreshold) "Movement Detected" else "No Movement")
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onCleared() {
        super.onCleared()
        stopAccelerometerTracking()
        stopAudioRecording()
    }

    @SuppressLint("NewApi")
    private fun hasExactAlarmPermission(): Boolean {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        return alarmManager.canScheduleExactAlarms()
    }

    @SuppressLint("InlinedApi")
    fun requestExactAlarmPermission() {
        val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }
}
