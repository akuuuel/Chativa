package com.chativa

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.chativa.data.remote.SupabaseConfig
import com.chativa.data.repository.PreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class ChativaApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
        initSupabaseConfig()
    }

    private fun initSupabaseConfig() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val pref = PreferencesRepository(this@ChativaApplication)
                val savedUrl = pref.supabaseUrlFlow.firstOrNull()
                val savedKey = pref.supabaseAnonKeyFlow.firstOrNull()
                if (!savedUrl.isNullOrBlank() && !savedKey.isNullOrBlank()) {
                    SupabaseConfig.updateConfig(savedUrl, savedKey)
                }
            } catch (_: Exception) {}
        }
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val messagesChannel = NotificationChannel(
                CHANNEL_MESSAGES,
                "Chat Messages",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifikasi pesan baru CHATIVA"
            }

            val callsChannel = NotificationChannel(
                CHANNEL_CALLS,
                "Incoming Calls",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifikasi panggilan masuk CHATIVA"
            }

            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(messagesChannel)
            manager.createNotificationChannel(callsChannel)
        }
    }

    companion object {
        const val CHANNEL_MESSAGES = "chativa_messages_channel"
        const val CHANNEL_CALLS = "chativa_calls_channel"
    }
}
