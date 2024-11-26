// AudioRecorder.kt
package com.example.sleepsafe.utils

import android.app.Application
import android.content.Context
import android.media.MediaRecorder
import android.os.Environment
import java.io.File
import java.io.IOException

class AudioRecorder(private val context: Context) {

    private var mediaRecorder: MediaRecorder? = null
    private var outputFile: String? = null

    fun startRecording(): String? {
        // Create a file in the external files directory
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

        // Return the output file path
        return outputFile
    }

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

    fun getMaxAmplitude(): Int {
        return mediaRecorder?.maxAmplitude ?: 0
    }
}
