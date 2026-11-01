package com.chativa.ui.navigation

import android.net.Uri

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Register : Screen("register")
    object ForgotPassword : Screen("forgot_password")
    object Dashboard : Screen("dashboard")
    object ChatRoom : Screen("chat_room/{chatId}/{peerName}/{peerId}") {
        fun createRoute(chatId: String, peerName: String, peerId: String): String {
            val encChatId = Uri.encode(chatId)
            val encPeerName = Uri.encode(peerName)
            val encPeerId = Uri.encode(peerId)
            return "chat_room/$encChatId/$encPeerName/$encPeerId"
        }
    }
    object AddContact : Screen("add_contact")
    object ContactDetail : Screen("contact_detail/{contactId}") {
        fun createRoute(contactId: String): String = "contact_detail/${Uri.encode(contactId)}"
    }
    object VoiceCall : Screen("voice_call/{peerName}") {
        fun createRoute(peerName: String): String = "voice_call/${Uri.encode(peerName)}"
    }
    object VideoCall : Screen("video_call/{peerName}") {
        fun createRoute(peerName: String): String = "video_call/${Uri.encode(peerName)}"
    }
    object AppSettings : Screen("app_settings")
    object ThemeSettings : Screen("theme_settings")
    object PrivacySettings : Screen("privacy_settings")
}
