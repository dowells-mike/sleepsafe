package com.example.sleepsafe.utils

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.sleepsafe.R
import com.example.sleepsafe.data.SleepData
import com.example.sleepsafe.data.SleepDatabase
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.coroutines.coroutineContext

class SleepTrackingService : Service() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private var motionDetector: MotionDetector? = null
    private var audioRecorder: AudioRecorder? = null
    private val sleepDao by lazy { SleepDatabase.getDatabase(applicationContext).sleepDao() }

    private var sleepStartTime: Long = 0
    private var alarmTime: Long = 0
    private var lastUpdateTime: Long = 0

    private var dataCollectionJob: Job? = null
    private var notificationJob: Job? = null

    companion object {
        private const val TAG = "SleepTrackingService"
        const val CHANNEL_ID = "SleepTrackingServiceChannel"
        const val NOTIFICATION_ID = 1

        const val EXTRA_SLEEP_START = "sleepStart"
        const val EXTRA_ALARM_TIME = "alarmTime"

        private const val DATA_UPDATE_INTERVAL = 5_000L // 5 seconds for testing
        private const val NOTIFICATION_UPDATE_INTERVAL = 5_000L // 5 seconds for testing

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

            // For testing, allow any future alarm time
            if (alarmTime > 0 && alarmTime <= System.currentTimeMillis()) {
                Log.e(TAG, "Invalid alarm time (in the past), stopping service")
                stopSelf()
                return START_NOT_STICKY
            }

            if (!checkPermissions()) {
                Log.e(TAG, "Missing required permissions")
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

    private fun checkPermissions(): Boolean {
        val audioPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
        val foregroundServicePermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ContextCompat.checkSelfPermission(this, Manifest.permission.FOREGROUND_SERVICE)
        } else {
            PackageManager.PERMISSION_GRANTED
        }

        return audioPermission == PackageManager.PERMISSION_GRANTED &&
                foregroundServicePermission == PackageManager.PERMISSION_GRANTED
    }

    private fun initializeService() {
        try {
            createNotificationChannel()
            startForeground(NOTIFICATION_ID, createNotification())

            motionDetector = MotionDetector(applicationContext)
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
            // Start motion detection
            if (motionDetector?.startDetecting() == true) {
                Log.d(TAG, "Motion detection started")
            } else {
                Log.e(TAG, "Failed to start motion detection")
            }

            // Start audio recording
            if (audioRecorder?.startRecording() == true) {
                Log.d(TAG, "Audio recording started")
            } else {
                Log.e(TAG, "Failed to start audio recording")
            }

            // Start collecting data with error handling
            dataCollectionJob = serviceScope.launch {
                try {
                    collectSleepData()
                } catch (e: Exception) {
                    if (e !is CancellationException) {
                        Log.e(TAG, "Error in data collection", e)
                    }
                }
            }

            // Start notification updates with error handling
            notificationJob = serviceScope.launch {
                try {
                    updateNotificationPeriodically()
                } catch (e: Exception) {
                    if (e !is CancellationException) {
                        Log.e(TAG, "Error in notification updates", e)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error starting tracking", e)
            stopSelf()
        }
    }

    private fun stopTracking() {
        try {
            // Cancel coroutines
            dataCollectionJob?.cancel()
            notificationJob?.cancel()
            serviceScope.cancel()
            Log.d(TAG, "Coroutines cancelled")

            // Stop motion detection
            motionDetector?.stopDetecting()
            Log.d(TAG, "Motion detection stopped")

            // Stop audio recording
            audioRecorder?.stopRecording()
            Log.d(TAG, "Audio recording stopped")

            // Clean up resources
            motionDetector = null
            audioRecorder = null

            Log.d(TAG, "Tracking stopped successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping tracking", e)
        }
    }

    private suspend fun collectSleepData() {
        while (coroutineContext.isActive) {
            try {
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
                    val motionLevel = motionDetector?.getMotionLevel() ?: 0f
                    val audioLevel = audioRecorder?.getMaxAmplitude() ?: 0f

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

                    lastUpdateTime = currentTime
                    Log.d(TAG, "Sleep data collected - Motion: $motionLevel, Audio: $audioLevel")
                }

                delay(1000) // Check every second
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Error collecting sleep data", e)
                delay(5000) // Wait before retrying
            }
        }
    }

    private suspend fun updateNotificationPeriodically() {
        while (coroutineContext.isActive) {
            try {
                val notification = createNotification()
                val notificationManager = getSystemService(NotificationManager::class.java)
                notificationManager.notify(NOTIFICATION_ID, notification)
                delay(NOTIFICATION_UPDATE_INTERVAL)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Error updating notification", e)
                delay(5000) // Wait before retrying
            }
        }
    }

    private fun createNotification(): Notification {
        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        val sleepDuration = System.currentTimeMillis() - sleepStartTime
        val hours = sleepDuration / (1000 * 60 * 60)
        val minutes = (sleepDuration % (1000 * 60 * 60)) / (1000 * 60)
        val seconds = (sleepDuration % (1000 * 60)) / 1000

        val contentText = buildString {
            append("Duration: ${hours}h ${minutes}m ${seconds}s")
            if (alarmTime > 0) {
                append("\nAlarm: ${timeFormat.format(Date(alarmTime))}")
            }
            append("\nMotion: ${motionDetector?.getMotionLevel()?.format(2) ?: "0.00"}")
            append("\nNoise: ${audioRecorder?.getMaxAmplitude()?.format(2) ?: "0.00"}")
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

    private fun Float.format(decimals: Int): String = "%.${decimals}f".format(this)
}
