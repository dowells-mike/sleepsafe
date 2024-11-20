package com.example.sleepsafe.viewmodel

import android.app.Application
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlin.math.sqrt

class HomeViewModel(application: Application) : AndroidViewModel(application), SensorEventListener {

    private val sensorManager: SensorManager =
        application.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private val _motionState = MutableLiveData<String>()
    val motionState: LiveData<String> get() = _motionState

    // Rolling average for smoother movement detection
    private var rollingAverage = 0f
    private val alpha = 0.8f // Smoothing factor (0.0 = no smoothing, 1.0 = full smoothing)

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

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || event.sensor.type != Sensor.TYPE_ACCELEROMETER) return

        // Calculate magnitude of acceleration
        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]
        val magnitude = sqrt(x * x + y * y + z * z)

        // Apply rolling average to smooth data
        rollingAverage = alpha * rollingAverage + (1 - alpha) * magnitude

        // Log the raw and smoothed values (for debugging)
        Log.d("HomeViewModel", "Raw: $magnitude, Smoothed: $rollingAverage")

        // Update state based on smoothed magnitude
        val movementThreshold = 2.0f // Increased threshold for better testing
        _motionState.postValue(
            if (rollingAverage > movementThreshold) "Movement Detected" else "No Movement"
        )
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No implementation needed
    }

    override fun onCleared() {
        super.onCleared()
        stopAccelerometerTracking()
    }
}
