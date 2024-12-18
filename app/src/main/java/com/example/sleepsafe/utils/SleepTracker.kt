// SleepTracker.kt
package com.example.sleepsafe.utils

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.util.Log
import kotlin.math.sqrt

/**
 * Handles sleep tracking logic with motion and sound analysis.
 * Uses a combination of accelerometer data and audio levels to determine sleep phases
 * and quality.
 */
class SleepTracker {
    companion object {
        private const val TAG = "SleepTracker"

        // Motion detection constants
        private const val MOTION_SENSITIVITY = 0.1f // Base sensitivity
        private const val MOTION_WINDOW_SIZE = 30 // Number of samples to consider
        private const val SIGNIFICANT_MOTION_THRESHOLD = 0.3f

        // Audio detection constants
        private const val NOISE_SENSITIVITY = 0.15f
        private const val NOISE_WINDOW_SIZE = 20
        private const val SNORING_THRESHOLD = 0.4f

        // Sleep phase detection
        private const val LIGHT_SLEEP_MOTION_THRESHOLD = 0.2f
        private const val DEEP_SLEEP_MOTION_THRESHOLD = 0.1f
        private const val REM_MOTION_VARIANCE_THRESHOLD = 0.25f
    }

    // Motion tracking
    private val motionWindow = ArrayDeque<Float>(MOTION_WINDOW_SIZE)
    private val gravity = FloatArray(3) { 0f }
    private var lastSignificantMotion = 0L
    private var lastMotionData = MotionData()

    // Audio tracking
    private val audioWindow = ArrayDeque<Float>(NOISE_WINDOW_SIZE)
    private var lastSnoreDetected = 0L
    private var lastAudioData = AudioData()

    // Sleep phase tracking
    private var currentPhase = SleepPhase.AWAKE
    private var phaseStartTime = System.currentTimeMillis()
    private var consecutiveDeepSleepReadings = 0
    private var consecutiveLightSleepReadings = 0
    private var consecutiveREMReadings = 0

    /**
     * Processes accelerometer data for motion detection.
     */
    fun processMotion(event: SensorEvent): MotionData {
        if (event.sensor.type != Sensor.TYPE_ACCELEROMETER) return lastMotionData

        // Apply low-pass filter to isolate gravity
        val alpha = 0.8f
        gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0]
        gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1]
        gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2]

        // Remove gravity contribution to get linear acceleration
        val linearAcceleration = FloatArray(3)
        linearAcceleration[0] = event.values[0] - gravity[0]
        linearAcceleration[1] = event.values[1] - gravity[1]
        linearAcceleration[2] = event.values[2] - gravity[2]

        // Calculate motion magnitude
        val magnitude = sqrt(
            linearAcceleration[0] * linearAcceleration[0] +
                    linearAcceleration[1] * linearAcceleration[1] +
                    linearAcceleration[2] * linearAcceleration[2]
        ) * MOTION_SENSITIVITY

        // Update motion window
        if (motionWindow.size >= MOTION_WINDOW_SIZE) {
            motionWindow.removeFirst()
        }
        motionWindow.addLast(magnitude)

        // Detect significant motion
        val avgMotion = motionWindow.average().toFloat()
        val isSignificantMotion = avgMotion > SIGNIFICANT_MOTION_THRESHOLD
        if (isSignificantMotion) {
            lastSignificantMotion = System.currentTimeMillis()
        }

        lastMotionData = MotionData(
            magnitude = magnitude,
            averageMotion = avgMotion,
            isSignificantMotion = isSignificantMotion
        )
        return lastMotionData
    }

    /**
     * Processes audio data for noise and snoring detection.
     */
    fun processAudio(amplitude: Float): AudioData {
        val normalizedAmplitude = amplitude * NOISE_SENSITIVITY

        // Update audio window
        if (audioWindow.size >= NOISE_WINDOW_SIZE) {
            audioWindow.removeFirst()
        }
        audioWindow.addLast(normalizedAmplitude)

        // Detect snoring pattern
        val isSnoring = detectSnoring(audioWindow)
        if (isSnoring) {
            lastSnoreDetected = System.currentTimeMillis()
        }

        lastAudioData = AudioData(
            amplitude = normalizedAmplitude,
            averageNoise = audioWindow.average().toFloat(),
            isSnoring = isSnoring
        )
        return lastAudioData
    }

    /**
     * Gets the last processed motion data.
     */
    fun getLastMotionData(): MotionData = lastMotionData

    /**
     * Gets the last processed audio data.
     */
    fun getLastAudioData(): AudioData = lastAudioData

    /**
     * Updates sleep phase based on motion and audio data.
     */
    fun updateSleepPhase(motionData: MotionData, audioData: AudioData): SleepPhase {
        val currentTime = System.currentTimeMillis()
        val timeInPhase = currentTime - phaseStartTime

        // Calculate motion variance
        val motionVariance = calculateVariance(motionWindow)

        // Determine sleep phase
        val newPhase = when {
            // Awake if significant motion or noise
            motionData.isSignificantMotion || audioData.averageNoise > SNORING_THRESHOLD -> {
                consecutiveDeepSleepReadings = 0
                consecutiveLightSleepReadings = 0
                consecutiveREMReadings = 0
                SleepPhase.AWAKE
            }

            // Deep sleep conditions
            motionData.averageMotion < DEEP_SLEEP_MOTION_THRESHOLD &&
                    audioData.averageNoise < NOISE_SENSITIVITY -> {
                consecutiveDeepSleepReadings++
                if (consecutiveDeepSleepReadings >= 10) SleepPhase.DEEP_SLEEP else currentPhase
            }

            // REM sleep conditions
            motionVariance > REM_MOTION_VARIANCE_THRESHOLD &&
                    timeInPhase > 45 * 60 * 1000 -> { // Minimum 45 minutes in previous phase
                consecutiveREMReadings++
                if (consecutiveREMReadings >= 5) SleepPhase.REM else currentPhase
            }

            // Light sleep conditions
            motionData.averageMotion < LIGHT_SLEEP_MOTION_THRESHOLD -> {
                consecutiveLightSleepReadings++
                if (consecutiveLightSleepReadings >= 5) SleepPhase.LIGHT_SLEEP else currentPhase
            }

            // Default to current phase if no clear condition is met
            else -> currentPhase
        }

        // Update phase if changed
        if (newPhase != currentPhase) {
            Log.d(TAG, "Sleep phase changed from $currentPhase to $newPhase")
            currentPhase = newPhase
            phaseStartTime = currentTime
        }

        return currentPhase
    }

    private fun detectSnoring(audioWindow: ArrayDeque<Float>): Boolean {
        if (audioWindow.size < NOISE_WINDOW_SIZE) return false

        // Look for rhythmic pattern in audio data
        val pattern = audioWindow.windowed(4, 1).map { window ->
            window[0] < window[1] && window[1] > window[2] && window[2] < window[3]
        }

        // Count pattern matches
        val patternMatches = pattern.count { it }
        return patternMatches >= 3 && audioWindow.average() > SNORING_THRESHOLD
    }

    private fun calculateVariance(window: ArrayDeque<Float>): Float {
        if (window.isEmpty()) return 0f
        val mean = window.average().toFloat()
        return window.map { (it - mean) * (it - mean) }.average().toFloat()
    }

    data class MotionData(
        val magnitude: Float = 0f,
        val averageMotion: Float = 0f,
        val isSignificantMotion: Boolean = false
    )

    data class AudioData(
        val amplitude: Float = 0f,
        val averageNoise: Float = 0f,
        val isSnoring: Boolean = false
    )

    enum class SleepPhase {
        AWAKE,
        LIGHT_SLEEP,
        DEEP_SLEEP,
        REM
    }
}
