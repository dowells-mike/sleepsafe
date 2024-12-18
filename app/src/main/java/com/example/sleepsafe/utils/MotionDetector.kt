package com.example.sleepsafe.utils

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import kotlin.math.sqrt

class MotionDetector(context: Context) : SensorEventListener {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

    private var running = false
    private var lastMotion = 0.0f
    private var lastAcceleration = FloatArray(3)
    private var lastGyro = FloatArray(3)
    private var gravity = FloatArray(3)

    // Moving average for smoothing
    private val accelerationWindow = FloatArray(WINDOW_SIZE) { 0f }
    private val gyroWindow = FloatArray(WINDOW_SIZE) { 0f }
    private var windowIndex = 0

    companion object {
        private const val TAG = "MotionDetector"
        private const val WINDOW_SIZE = 10
        private const val ALPHA = 0.8f // Low-pass filter constant
        private const val MOTION_AMPLIFICATION = 3.0f // Amplify motion for better visibility
    }

    @Synchronized
    fun startDetecting(): Boolean {
        if (running) {
            return true
        }

        try {
            // Register accelerometer
            accelerometer?.let { sensor ->
                sensorManager.registerListener(
                    this,
                    sensor,
                    SensorManager.SENSOR_DELAY_GAME // Faster updates for better detection
                )
                Log.d(TAG, "Accelerometer registered")
            }

            // Register gyroscope if available
            gyroscope?.let { sensor ->
                sensorManager.registerListener(
                    this,
                    sensor,
                    SensorManager.SENSOR_DELAY_GAME
                )
                Log.d(TAG, "Gyroscope registered")
            }

            running = true
            Log.d(TAG, "Motion detection started")
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Error starting motion detection", e)
            stopDetecting()
            return false
        }
    }

    @Synchronized
    fun stopDetecting() {
        running = false
        try {
            sensorManager.unregisterListener(this)
            lastMotion = 0.0f
            lastAcceleration = FloatArray(3)
            lastGyro = FloatArray(3)
            gravity = FloatArray(3)
            accelerationWindow.fill(0f)
            gyroWindow.fill(0f)
            windowIndex = 0
            Log.d(TAG, "Motion detection stopped")
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping motion detection", e)
        }
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (!running) return

        try {
            when (event.sensor.type) {
                Sensor.TYPE_ACCELEROMETER -> processAccelerometer(event)
                Sensor.TYPE_GYROSCOPE -> processGyroscope(event)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error processing sensor data", e)
        }
    }

    private fun processAccelerometer(event: SensorEvent) {
        // Apply low-pass filter to isolate gravity
        gravity[0] = ALPHA * gravity[0] + (1 - ALPHA) * event.values[0]
        gravity[1] = ALPHA * gravity[1] + (1 - ALPHA) * event.values[1]
        gravity[2] = ALPHA * gravity[2] + (1 - ALPHA) * event.values[2]

        // Remove gravity contribution to get linear acceleration
        val linearAcceleration = FloatArray(3)
        linearAcceleration[0] = event.values[0] - gravity[0]
        linearAcceleration[1] = event.values[1] - gravity[1]
        linearAcceleration[2] = event.values[2] - gravity[2]

        // Calculate magnitude of acceleration
        val magnitude = sqrt(
            linearAcceleration[0] * linearAcceleration[0] +
                    linearAcceleration[1] * linearAcceleration[1] +
                    linearAcceleration[2] * linearAcceleration[2]
        )

        // Add to moving average window
        accelerationWindow[windowIndex] = magnitude
    }

    private fun processGyroscope(event: SensorEvent) {
        // Calculate magnitude of rotation
        val magnitude = sqrt(
            event.values[0] * event.values[0] +
                    event.values[1] * event.values[1] +
                    event.values[2] * event.values[2]
        )

        // Add to moving average window
        gyroWindow[windowIndex] = magnitude

        // Update window index
        windowIndex = (windowIndex + 1) % WINDOW_SIZE

        // Calculate combined motion value
        updateMotionValue()
    }

    private fun updateMotionValue() {
        // Calculate averages
        val avgAcceleration = accelerationWindow.average().toFloat()
        val avgGyro = gyroWindow.average().toFloat()

        // Combine acceleration and gyro data
        // Weight acceleration more as it's more relevant for sleep movement
        val combinedMotion = if (gyroscope != null) {
            (avgAcceleration * 0.7f + avgGyro * 0.3f)
        } else {
            avgAcceleration
        }

        // Amplify and normalize to 0-1 range
        lastMotion = (combinedMotion * MOTION_AMPLIFICATION).coerceIn(0f, 1f)
        Log.d(TAG, "Motion level: $lastMotion")
    }

    @Synchronized
    fun getMotionLevel(): Float {
        return if (running) lastMotion else 0.0f
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not needed
    }
}
