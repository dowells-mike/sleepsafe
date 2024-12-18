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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.sqrt

/**
 * Service for tracking sleep using accelerometer and audio data.
 * Runs in the background as a foreground service.
 */
class SleepTrackingService : Service(), SensorEventListener {

    private val job = Job()
    private val coroutineScope = CoroutineScope(Dispatchers.IO + job)

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private lateinit var audioRecorder: AudioRecorder
    private val sleepDao by lazy { SleepDatabase.getDatabase(applicationContext).sleepDao() }

    private var gravity = FloatArray(3) { 0f }
    private var linearAcceleration = FloatArray(3) { 0f }

    private var sleepStartTime: Long = 0
    private var alarmTime: Long = 0

    companion object {
        private const val TAG = "SleepTrackingService"
        const val CHANNEL_ID = "SleepTrackingServiceChannel"
        private const val ACCELEROMETER_UPDATE_INTERVAL = 1000L // 1 second
        private const val AUDIO_UPDATE_INTERVAL = 1000L // 1 second

        // Intent extra keys
        const val EXTRA_SLEEP_START = "sleepStart"
        const val EXTRA_ALARM_TIME = "alarmTime"

        /**
         * Starts the SleepTrackingService.
         */
        fun startService(context: Context, intent: Intent) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
            Log.d(TAG, "Service start requested with intent: $intent")
        }

        /**
         * Stops the SleepTrackingService.
         */
        fun stopService(context: Context) {
            val stopIntent = Intent(context, SleepTrackingService::class.java)
            context.stopService(stopIntent)
            Log.d(TAG, "Service stop requested")
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service onCreate")
        initializeService()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service onStartCommand")

        if (intent == null) {
            Log.e(TAG, "Received null intent, stopping service")
            stopSelf()
            return START_NOT_STICKY
        }

        try {
            sleepStartTime = intent.getLongExtra(EXTRA_SLEEP_START, System.currentTimeMillis())
            alarmTime = intent.getLongExtra(EXTRA_ALARM_TIME, 0L)

            Log.d(TAG, "Received sleep start time: $sleepStartTime, alarm time: $alarmTime")

            if (alarmTime > 0 && alarmTime <= sleepStartTime) {
                Log.e(TAG, "Invalid alarm time (before sleep start), stopping service")
                stopSelf()
                return START_NOT_STICKY
            }

            startTracking()
            return START_STICKY
        } catch (e: Exception) {
            Log.e(TAG, "Error in onStartCommand", e)
            stopSelf()
            return START_NOT_STICKY
        }
    }

    private fun initializeService() {
        try {
            createNotificationChannel()
            startForeground(1, createNotification())

            sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            audioRecorder = AudioRecorder(applicationContext)

            Log.d(TAG, "Service initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing service", e)
            stopSelf()
        }
    }

    override fun onDestroy() {
        Log.d(TAG, "Service onDestroy")
        stopTracking()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startTracking() {
        try {
            // Start accelerometer tracking
            accelerometer?.let {
                sensorManager.registerListener(
                    this,
                    it,
                    SensorManager.SENSOR_DELAY_NORMAL,
                    ACCELEROMETER_UPDATE_INTERVAL.toInt() * 1000
                )
                Log.d(TAG, "Accelerometer tracking started")
            } ?: Log.e(TAG, "No accelerometer available")

            // Start audio recording
            audioRecorder.startRecording()
            Log.d(TAG, "Audio recording started")

            // Start collecting data
            coroutineScope.launch { collectSleepData() }
        } catch (e: Exception) {
            Log.e(TAG, "Error starting tracking", e)
            stopSelf()
        }
    }

    private fun stopTracking() {
        try {
            // Stop accelerometer tracking
            sensorManager.unregisterListener(this)

            // Stop audio recording
            audioRecorder.stopRecording()

            // Cancel the coroutine
            job.cancel()

            Log.d(TAG, "Tracking stopped successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping tracking", e)
        }
    }

    private suspend fun collectSleepData() {
        try {
            while (true) {
                val currentTime = System.currentTimeMillis()

                // Check if we've reached the alarm time
                if (alarmTime > 0 && currentTime >= alarmTime) {
                    Log.d(TAG, "Reached alarm time, stopping service")
                    withContext(Dispatchers.Main) {
                        stopSelf()
                    }
                    break
                }

                val audioLevel = audioRecorder.getMaxAmplitude().toFloat()
                val motionLevel = calculateMotionLevel()

                val sleepData = SleepData(
                    timestamp = currentTime,
                    motion = motionLevel,
                    audioLevel = audioLevel,
                    sleepStart = sleepStartTime,
                    alarmTime = alarmTime
                )

                withContext(Dispatchers.IO) {
                    sleepDao.insert(sleepData)
                }

                Log.d(TAG, "Data collected - Time: $currentTime, Motion: $motionLevel, Audio: $audioLevel")
                delay(AUDIO_UPDATE_INTERVAL)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error collecting sleep data", e)
            withContext(Dispatchers.Main) {
                stopSelf()
            }
        }
    }

    private fun calculateMotionLevel(): Float {
        return sqrt(
            linearAcceleration[0] * linearAcceleration[0] +
                    linearAcceleration[1] * linearAcceleration[1] +
                    linearAcceleration[2] * linearAcceleration[2]
        )
    }

    private fun createNotification(): Notification {
        val formattedStartTime = android.text.format.DateFormat.getTimeFormat(this)
            .format(sleepStartTime)

        val formattedAlarmTime = if (alarmTime > 0) {
            android.text.format.DateFormat.getTimeFormat(this).format(alarmTime)
        } else {
            "Not Set"
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Sleep Tracking Active")
            .setContentText("Start: $formattedStartTime | Alarm: $formattedAlarmTime")
            .setSmallIcon(R.drawable.ic_sleep)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(Notification.CATEGORY_SERVICE)
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Sleep Tracking Service Channel",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Tracks sleep patterns using device sensors"
                setShowBadge(false)
            }

            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type != Sensor.TYPE_ACCELEROMETER) return

        // Low-pass filter to separate gravity from linear acceleration
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
