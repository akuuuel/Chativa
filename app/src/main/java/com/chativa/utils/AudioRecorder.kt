package com.chativa.utils

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import java.io.File

class AudioRecorder(private val context: Context) {

    private var recorder: MediaRecorder? = null
    private var startTimeMs: Long = 0

    fun start(outputFile: File): Boolean {
        return try {
            recorder = createRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(outputFile.absolutePath)
                prepare()
                start()
            }
            startTimeMs = System.currentTimeMillis()
            true
        } catch (e: Exception) {
            Log.e("CHATIVA_AUDIO", "Error starting MediaRecorder", e)
            recorder?.release()
            recorder = null
            false
        }
    }

    fun stop(): Int {
        val durationSec = ((System.currentTimeMillis() - startTimeMs) / 1000).toInt()
        try {
            recorder?.stop()
            recorder?.release()
        } catch (e: Exception) {
            Log.e("CHATIVA_AUDIO", "Error stopping MediaRecorder", e)
        }
        recorder = null
        return durationSec.coerceAtLeast(1)
    }

    fun cancel() {
        try {
            recorder?.stop()
        } catch (_: Exception) {}
        try {
            recorder?.release()
        } catch (_: Exception) {}
        recorder = null
    }

    @Suppress("DEPRECATION")
    private fun createRecorder(): MediaRecorder {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            MediaRecorder()
        }
    }
}
