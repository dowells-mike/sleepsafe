package com.example.sleepsafe.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import androidx.core.content.ContextCompat
import kotlin.math.sqrt

class AudioRecorder(private val context: Context) {
    private var audioRecord: AudioRecord? = null
    private var recordingThread: Thread? = null
    private var lastAmplitude = 0.0f
    private var running = false
    private var sensitivity = 0.5f // Default sensitivity (range 0.1-1.0)

    companion object {
        private const val TAG = "AudioRecorder"
        private const val SAMPLE_RATE = 44100 // Higher sample rate for better quality
        private const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
        private const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
        private const val BUFFER_SIZE_FACTOR = 4 // Larger buffer for smoother readings
        private val BUFFER_SIZE = AudioRecord.getMinBufferSize(
            SAMPLE_RATE,
            CHANNEL_CONFIG,
            AUDIO_FORMAT
        ) * BUFFER_SIZE_FACTOR
    }

    fun setSensitivity(value: Float) {
        sensitivity = value.coerceIn(0.1f, 1.0f)
        Log.d(TAG, "Audio sensitivity set to: $sensitivity")
    }

    @Synchronized
    fun startRecording(): Boolean {
        if (running) {
            return true
        }

        if (!checkPermission()) {
            Log.e(TAG, "Missing RECORD_AUDIO permission")
            return false
        }

        try {
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.VOICE_RECOGNITION, // Better for voice detection
                SAMPLE_RATE,
                CHANNEL_CONFIG,
                AUDIO_FORMAT,
                BUFFER_SIZE
            )

            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                Log.e(TAG, "AudioRecord failed to initialize")
                return false
            }

            audioRecord?.startRecording()
            running = true

            recordingThread = Thread({
                val audioBuffer = ShortArray(BUFFER_SIZE / 2)
                var readSize: Int

                while (running) {
                    try {
                        readSize = audioRecord?.read(audioBuffer, 0, audioBuffer.size) ?: 0
                        if (readSize > 0) {
                            // Calculate RMS (Root Mean Square) amplitude
                            var sum = 0.0
                            for (i in 0 until readSize) {
                                sum += audioBuffer[i] * audioBuffer[i]
                            }
                            val rms = sqrt(sum / readSize)

                            // Apply sensitivity and normalize to 0-1 range
                            lastAmplitude = ((rms / Short.MAX_VALUE) * sensitivity * 5).toFloat().coerceIn(0f, 1f)

                            Log.d(TAG, "Audio level: $lastAmplitude")
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error reading audio data", e)
                        if (!running) break // Only break if we're supposed to stop
                        Thread.sleep(100) // Brief pause before retrying
                    }
                }
            }, "AudioRecordingThread").apply {
                priority = Thread.MAX_PRIORITY
                start()
            }

            Log.d(TAG, "Audio recording started successfully")
            return true
        } catch (e: SecurityException) {
            Log.e(TAG, "Security exception starting audio recording", e)
            return false
        } catch (e: Exception) {
            Log.e(TAG, "Error starting audio recording", e)
            stopRecording()
            return false
        }
    }

    @Synchronized
    fun stopRecording() {
        running = false
        try {
            recordingThread?.join(1000)
            audioRecord?.stop()
            audioRecord?.release()
            audioRecord = null
            recordingThread = null
            lastAmplitude = 0.0f
            Log.d(TAG, "Audio recording stopped")
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping audio recording", e)
        }
    }

    @Synchronized
    fun getMaxAmplitude(): Float {
        return if (running) lastAmplitude else 0.0f
    }

    private fun checkPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }
}
