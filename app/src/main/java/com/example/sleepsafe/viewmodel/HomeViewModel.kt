package com.example.sleepsafe.viewmodel

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.Application
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import android.app.PendingIntent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.sleepsafe.utils.AlarmReceiver
import com.example.sleepsafe.utils.AudioRecorder
import java.util.Calendar
import kotlin.math.sqrt

@SuppressLint("StaticFieldLeak")
class HomeViewModel(application: Application) : AndroidViewModel(application), SensorEventListener {

    private val sensorManager: SensorManager =
        application.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private val _motionState = MutableLiveData<String>()
    val motionState: LiveData<String> get() = _motionState

    private val audioRecorder = AudioRecorder(application)
    private val _isRecording = MutableLiveData<Boolean>()
    val isRecording: LiveData<Boolean> get() = _isRecording

    private val _audioFilePath = MutableLiveData<String?>()
    val audioFilePath: LiveData<String?> get() = _audioFilePath

    private val gravity = FloatArray(3) { 0f }
    private val linearAcceleration = FloatArray(3) { 0f }

    private val context: Context = getApplication<Application>().applicationContext
    private val _alarmTime = MutableLiveData<Long?>()
    val alarmTime: LiveData<Long?> get() = _alarmTime

    private val _permissionRequired = MutableLiveData<Boolean>()
    val permissionRequired: LiveData<Boolean> get() = _permissionRequired

    init {
        startAccelerometerTracking()
    }

    private fun startAccelerometerTracking() {
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    private fun stopAccelerometerTracking() {
        sensorManager.unregisterListener(this)
    }

    fun startAudioRecording() {
        try {
            _isRecording.postValue(true)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                _audioFilePath.postValue(audioRecorder.startRecording())
            }
        } catch (e: Exception) {
            _isRecording.postValue(false)
            e.printStackTrace()
        }
    }

    fun stopAudioRecording() {
        _isRecording.postValue(false)
        audioRecorder.stopRecording()
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
        _motionState.postValue(
            if (magnitude > movementThreshold) "Movement Detected" else "No Movement"
        )
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onCleared() {
        super.onCleared()
        stopAccelerometerTracking()
        stopAudioRecording()
    }

    fun setAlarm(hour: Int, minute: Int, useSmartAlarm: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!hasExactAlarmPermission()) {
                // Set permissionRequired only when permission is not granted
                _permissionRequired.postValue(true)
                return
            } else {
                // Reset permissionRequired if permission is granted
                _permissionRequired.postValue(false)
            }
        }

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            if (before(Calendar.getInstance())) {
                add(Calendar.DAY_OF_YEAR, 1) // Schedule for the next day if time is past
            }
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("useSmartAlarm", useSmartAlarm)
            putExtra("alarmTime", calendar.timeInMillis)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )

        _alarmTime.postValue(calendar.timeInMillis)
        Log.d("HomeViewModel", "Alarm set for: ${calendar.time}")
    }

    fun cancelAlarm() {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
        _alarmTime.postValue(null)
    }

    @SuppressLint("NewApi")
    fun hasExactAlarmPermission(): Boolean {
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
