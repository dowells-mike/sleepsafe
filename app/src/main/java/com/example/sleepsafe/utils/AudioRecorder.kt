package com.example.sleepsafe.utils

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.os.Environment
import androidx.annotation.RequiresApi
import java.io.File
import java.io.IOException

class AudioRecorder(private val context: Context) {

    private var mediaRecorder: MediaRecorder? = null
    private var outputFile: String? = null

    @RequiresApi(Build.VERSION_CODES.S)
    fun startRecording(): String {
        // Create a file in the external cache directory
        val audioFile = File(context.getExternalFilesDir(Environment.DIRECTORY_MUSIC), "sleep_audio_${System.currentTimeMillis()}.3gp")
        outputFile = audioFile.absolutePath

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            mediaRecorder = MediaRecorder(context).apply {
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
        }
        return outputFile!!
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

    fun getOutputFile(): String? {
        return outputFile
    }
}
