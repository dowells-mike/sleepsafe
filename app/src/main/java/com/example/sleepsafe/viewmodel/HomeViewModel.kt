package com.example.sleepsafe.viewmodel

import android.app.Application
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlin.math.sqrt

class HomeViewModel(application: Application) : AndroidViewModel(application), SensorEventListener {

    // Sensor manager and accelerometer
    private val sensorManager: SensorManager =
        application.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    // LiveData to expose the motion state (e.g., movement detected)
    private val _motionState = MutableLiveData<String>()
    val motionState: LiveData<String> get() = _motionState

    init {
        startAccelerometerTracking()
    }

    // Start listening to accelerometer events
    private fun startAccelerometerTracking() {
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    // Stop listening to accelerometer events (cleanup when ViewModel is cleared)
    private fun stopAccelerometerTracking() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || event.sensor.type != Sensor.TYPE_ACCELEROMETER) return

        // Calculate movement magnitude
        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]
        val magnitude = sqrt((x * x + y * y + z * z).toDouble()).toFloat()

        // Update motion state based on a threshold
        val movementThreshold = 1.5f
        _motionState.postValue(
            if (magnitude > movementThreshold) "Movement Detected" else "No Movement"
        )
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // We don't need to handle accuracy changes in this implementation
    }

    override fun onCleared() {
        super.onCleared()
        stopAccelerometerTracking()
    }
}
