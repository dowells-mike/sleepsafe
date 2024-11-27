// AudioRecorder.kt
package com.example.sleepsafe.utils

import android.content.Context
import android.media.MediaRecorder
import android.os.Environment
import java.io.File
import java.io.IOException

/**
 * A utility class for recording audio during sleep tracking.
 *
 * @param context The application context for accessing file storage.
 */
class AudioRecorder(private val context: Context) {

    private var mediaRecorder: MediaRecorder? = null
    private var outputFile: String? = null

    /**
     * Starts audio recording and returns the output file path.
     *
     * @return The absolute path of the recorded audio file.
     */
    fun startRecording(): String? {
        val audioFile = File(
            context.getExternalFilesDir(Environment.DIRECTORY_MUSIC),
            "sleep_audio_${System.currentTimeMillis()}.3gp"
        )
        outputFile = audioFile.absolutePath

        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            setOutputFile(outputFile)

            try {
                prepare()
                start()
            } catch (e: IOException) {
                e.printStackTrace()
                throw RuntimeException("Error starting MediaRecorder", e)
            }
        }

        return outputFile
    }

    /**
     * Stops audio recording and releases resources.
     */
    fun stopRecording() {
        mediaRecorder?.apply {
            try {
                stop()
                release()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        mediaRecorder = null
    }

    /**
     * Returns the maximum amplitude recorded since the last call to this method.
     *
     * @return The maximum amplitude value.
     */
    fun getMaxAmplitude(): Int {
        return mediaRecorder?.maxAmplitude ?: 0
    }
}
