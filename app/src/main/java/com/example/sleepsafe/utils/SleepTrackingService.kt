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
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*

class SleepTrackingService : Service(), SensorEventListener {
    private val job = Job()
    private val serviceScope = CoroutineScope(Dispatchers.IO + job)

    private var sensorManager: SensorManager? = null
    private var accelerometer: Sensor? = null
    private var audioRecorder: AudioRecorder? = null
    private val sleepDao by lazy { SleepDatabase.getDatabase(applicationContext).sleepDao() }
    private var sleepTracker: SleepTracker? = null

    private var sleepStartTime: Long = 0
    private var alarmTime: Long = 0
    private var lastUpdateTime: Long = 0
    private var currentPhase: SleepTracker.SleepPhase = SleepTracker.SleepPhase.AWAKE

    companion object {
        private const val TAG = "SleepTrackingService"
        const val CHANNEL_ID = "SleepTrackingServiceChannel"
        const val NOTIFICATION_ID = 1

        const val EXTRA_SLEEP_START = "sleepStart"
        const val EXTRA_ALARM_TIME = "alarmTime"

        private const val DATA_UPDATE_INTERVAL = 30_000L // 30 seconds
        private const val NOTIFICATION_UPDATE_INTERVAL = 60_000L // 1 minute

        fun startService(context: Context, intent: Intent) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
            Log.d(TAG, "Service start requested")
        }

        fun stopService(context: Context) {
            context.stopService(Intent(context, SleepTrackingService::class.java))
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
            startForeground(NOTIFICATION_ID, createNotification())

            sensorManager = getSystemService(Context.SENSOR_SERVICE) as? SensorManager
            if (sensorManager == null) {
                Log.e(TAG, "Failed to get SensorManager")
                stopSelf()
                return
            }

            accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            if (accelerometer == null) {
                Log.e(TAG, "No accelerometer available")
            }

            audioRecorder = AudioRecorder(applicationContext)
            sleepTracker = SleepTracker()

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
            accelerometer?.let { sensor ->
                sensorManager?.registerListener(
                    this,
                    sensor,
                    SensorManager.SENSOR_DELAY_NORMAL
                )
                Log.d(TAG, "Accelerometer tracking started")
            }

            // Start audio recording
            audioRecorder?.startRecording()
            Log.d(TAG, "Audio recording started")

            // Start collecting data
            serviceScope.launch { collectSleepData() }

            // Start notification updates
            serviceScope.launch { updateNotificationPeriodically() }
        } catch (e: Exception) {
            Log.e(TAG, "Error starting tracking", e)
            stopSelf()
        }
    }

    private fun stopTracking() {
        try {
            // Stop accelerometer tracking
            sensorManager?.unregisterListener(this)
            Log.d(TAG, "Accelerometer tracking stopped")

            // Stop audio recording
            audioRecorder?.stopRecording()
            Log.d(TAG, "Audio recording stopped")

            // Cancel all coroutines
            job.cancel()
            Log.d(TAG, "Coroutines cancelled")

            // Clean up resources
            audioRecorder = null
            sleepTracker = null

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

                // Only update if enough time has passed
                if (currentTime - lastUpdateTime >= DATA_UPDATE_INTERVAL) {
                    sleepTracker?.let { tracker ->
                        val motionData = tracker.getLastMotionData()
                        val audioLevel = audioRecorder?.getMaxAmplitude()?.toFloat() ?: 0f
                        val audioData = tracker.processAudio(audioLevel)

                        currentPhase = tracker.updateSleepPhase(motionData, audioData)

                        val sleepData = SleepData(
                            timestamp = currentTime,
                            motion = motionData.magnitude,
                            audioLevel = audioData.amplitude,
                            sleepStart = sleepStartTime,
                            alarmTime = alarmTime,
                            sleepPhase = currentPhase.name
                        )

                        withContext(Dispatchers.IO) {
                            sleepDao.insert(sleepData)
                        }

                        lastUpdateTime = currentTime
                        Log.d(TAG, "Sleep data collected: $sleepData")
                    }
                }

                delay(1000) // Check every second
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error collecting sleep data", e)
            withContext(Dispatchers.Main) {
                stopSelf()
            }
        }
    }

    private suspend fun updateNotificationPeriodically() {
        try {
            while (true) {
                val notification = createNotification()
                val notificationManager = getSystemService(NotificationManager::class.java)
                notificationManager.notify(NOTIFICATION_ID, notification)
                delay(NOTIFICATION_UPDATE_INTERVAL)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating notification", e)
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type != Sensor.TYPE_ACCELEROMETER) return

        try {
            sleepTracker?.processMotion(event)
        } catch (e: Exception) {
            Log.e(TAG, "Error processing sensor data", e)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No action needed
    }

    private fun createNotification(): Notification {
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val sleepDuration = System.currentTimeMillis() - sleepStartTime
        val hours = sleepDuration / (1000 * 60 * 60)
        val minutes = (sleepDuration % (1000 * 60 * 60)) / (1000 * 60)

        val contentText = buildString {
            append("Sleep Phase: $currentPhase")
            append("\nDuration: ${hours}h ${minutes}m")
            if (alarmTime > 0) {
                append("\nAlarm: ${timeFormat.format(Date(alarmTime))}")
            }
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Sleep Tracking Active")
            .setContentText(contentText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(contentText))
            .setSmallIcon(R.drawable.ic_sleep)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Sleep Tracking Service Channel",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows sleep tracking status"
                setShowBadge(false)
            }

            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}
