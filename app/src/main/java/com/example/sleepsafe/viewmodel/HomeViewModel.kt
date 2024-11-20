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

    // Variables for gravity removal (high-pass filter)
    private val gravity = FloatArray(3) { 0f }
    private val linearAcceleration = FloatArray(3) { 0f }

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

        // Isolate the force of gravity using a low-pass filter
        val alpha = 0.8f // Smoothing factor for gravity
        gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0]
        gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1]
        gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2]

        // Remove gravity to get linear acceleration
        linearAcceleration[0] = event.values[0] - gravity[0]
        linearAcceleration[1] = event.values[1] - gravity[1]
        linearAcceleration[2] = event.values[2] - gravity[2]

        // Calculate the magnitude of linear acceleration (movement)
        val magnitude = sqrt(
            linearAcceleration[0] * linearAcceleration[0] +
                    linearAcceleration[1] * linearAcceleration[1] +
                    linearAcceleration[2] * linearAcceleration[2]
        )

        // Log raw and filtered values for debugging
        Log.d("HomeViewModel", "Filtered Magnitude: $magnitude")

        // Update motion state based on a refined threshold
        val movementThreshold = 1.5f // Refined threshold for real movement
        _motionState.postValue(
            if (magnitude > movementThreshold) "Movement Detected" else "No Movement"
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
