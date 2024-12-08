// SleepTrackingService.kt
package com.example.sleepsafe.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.sleepsafe.R
import com.example.sleepsafe.data.SleepData
import com.example.sleepsafe.data.SleepDatabase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlin.coroutines.coroutineContext
import kotlin.math.sqrt

/**
 * Service for tracking sleep using accelerometer and audio data.
 * Runs in the background as a foreground service.
 */
class SleepTrackingService : Service(), SensorEventListener {

    private val coroutineScope = CoroutineScope(Dispatchers.Default)
    private val sleepDataList = mutableListOf<SleepData>()

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private lateinit var audioRecorder: AudioRecorder
    private val sleepDao by lazy { SleepDatabase.getDatabase(applicationContext).sleepDao() }

    private var gravity = FloatArray(3) { 0f }
    private var linearAcceleration = FloatArray(3) { 0f }

    companion object {
        const val CHANNEL_ID = "SleepTrackingServiceChannel"

        /**
         * Starts the SleepTrackingService.
         */
        fun startService(context: Context) {
            val startIntent = Intent(context, SleepTrackingService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(startIntent)
            } else {
                context.startService(startIntent)
            }
        }

        /**
         * Stops the SleepTrackingService.
         */
        fun stopService(context: Context) {
            val stopIntent = Intent(context, SleepTrackingService::class.java)
            context.stopService(stopIntent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(1, getNotification())
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        audioRecorder = AudioRecorder(context = applicationContext)

        startTracking()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopTracking()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startTracking() {
        // Start accelerometer tracking
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
        // Start audio recording
        audioRecorder.startRecording()
        // Start collecting data
        coroutineScope.launch { collectSleepData() }
    }

    private fun stopTracking() {
        // Stop accelerometer tracking
        sensorManager.unregisterListener(this)
        // Stop audio recording
        audioRecorder.stopRecording()
        // Save data and clean up
        runBlocking { saveSleepData() }
        coroutineScope.cancel()
    }

    private suspend fun saveSleepData() {
        withContext(Dispatchers.IO) {
            sleepDao.insertAll(sleepDataList)
            Log.d(
                "SleepTrackingService",
                "Saved ${sleepDataList.size} data points to the database."
            )
        }
    }

    private suspend fun collectSleepData() {
        try {
            while (coroutineContext.isActive) {
                val currentTime = System.currentTimeMillis()
                val audioLevel = audioRecorder.getMaxAmplitude()
                val motionLevel = calculateMotionLevel()

                val sleepData = SleepData(
                    timestamp = currentTime,
                    motion = motionLevel,
                    audioLevel = audioLevel.toFloat()
                )
                sleepDataList.add(sleepData)

                // Save data to database
                coroutineScope.launch {
                    sleepDao.insertAll(sleepDataList)
                    Log.d("SleepTrackingService", "Saved ${sleepDataList.size} data points to the database.")
                    sleepDataList.clear()
                }

                delay(1000) // Collect data every second
            }
        } catch (e: CancellationException) {
            Log.d("SleepTrackingService", "Data collection coroutine was cancelled.")
        }
    }

    private fun calculateMotionLevel(): Float {
        return sqrt(
            linearAcceleration[0] * linearAcceleration[0] +
                    linearAcceleration[1] * linearAcceleration[1] +
                    linearAcceleration[2] * linearAcceleration[2]
        )
    }

    private fun getNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("SleepSafe is tracking your sleep")
            .setSmallIcon(R.drawable.ic_sleep)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(Notification.CATEGORY_SERVICE)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Sleep Tracking Service Channel",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
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
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No action needed
    }
}
