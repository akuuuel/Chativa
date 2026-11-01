package com.chativa.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.chativa.data.model.*
import com.chativa.data.remote.SupabaseConfig
import com.chativa.data.repository.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val preferencesRepository = PreferencesRepository(application)
    val chatRepository = ChatRepository()
    val contactRepository = ContactRepository()
    val callRepository = CallRepository()

    val themeSettings: StateFlow<ThemeSettings> = preferencesRepository.themeSettingsFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, ThemeSettings())

    val privacySettings: StateFlow<PrivacySettings> = preferencesRepository.privacySettingsFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, PrivacySettings())

    val supabaseUrl: StateFlow<String> = preferencesRepository.supabaseUrlFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, SupabaseConfig.SUPABASE_URL)

    val supabaseAnonKey: StateFlow<String> = preferencesRepository.supabaseAnonKeyFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, SupabaseConfig.SUPABASE_ANON_KEY)

    val chats: StateFlow<List<Chat>> = chatRepository.chats
    val contacts: StateFlow<List<Contact>> = contactRepository.contacts
    val callLogs: StateFlow<List<CallLog>> = callRepository.callLogs
    val activeCall: StateFlow<ActiveCallState?> = callRepository.activeCall

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateSupabaseConfig(url: String, key: String) {
        viewModelScope.launch {
            preferencesRepository.updateSupabaseConfig(url, key)
        }
    }

    // Theme operations
    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { preferencesRepository.updateThemeMode(mode) }
    }

    fun setPrimaryColor(hex: String) {
        viewModelScope.launch { preferencesRepository.updatePrimaryColor(hex) }
    }

    fun setSecondaryColor(hex: String) {
        viewModelScope.launch { preferencesRepository.updateSecondaryColor(hex) }
    }

    fun setChatBubbleColor(hex: String) {
        viewModelScope.launch { preferencesRepository.updateChatBubbleColor(hex) }
    }

    fun setChatWallpaper(wallpaper: String) {
        viewModelScope.launch { preferencesRepository.updateChatWallpaper(wallpaper) }
    }

    fun setSoundEnabled(enabled: Boolean) {
        viewModelScope.launch { preferencesRepository.updateSoundEnabled(enabled) }
    }

    fun setVibrationEnabled(enabled: Boolean) {
        viewModelScope.launch { preferencesRepository.updateVibrationEnabled(enabled) }
    }

    // Privacy operations
    fun setPrivacyOnline(option: PrivacyOption) {
        viewModelScope.launch { preferencesRepository.updatePrivacyOnline(option) }
    }

    fun setPrivacyLastSeen(option: PrivacyOption) {
        viewModelScope.launch { preferencesRepository.updatePrivacyLastSeen(option) }
    }

    fun setPrivacyProfilePic(option: PrivacyOption) {
        viewModelScope.launch { preferencesRepository.updatePrivacyProfilePic(option) }
    }

    fun setReadReceipts(enabled: Boolean) {
        viewModelScope.launch { preferencesRepository.updateReadReceipts(enabled) }
    }

    // Contact operations
    suspend fun addContact(name: String, phone: String, email: String = ""): Contact {
        return contactRepository.addContact(name, phone, email)
    }

    fun updateContact(contactId: String, name: String, phone: String, email: String) {
        contactRepository.updateContact(contactId, name, phone, email)
    }

    fun deleteContact(contactId: String) {
        contactRepository.deleteContact(contactId)
    }

    // Chat operations
    fun startListeningToUserChats(userId: String) {
        if (userId.isNotBlank() && userId != "current_user") {
            chatRepository.startListeningToUserChats(userId)
        }
    }

    fun sendMessage(
        chatId: String,
        senderId: String,
        receiverId: String,
        senderName: String = "Saya",
        text: String,
        type: MessageType = MessageType.TEXT,
        mediaUrl: String = "",
        fileName: String = "",
        fileSize: String = "",
        voiceDurationSec: Int = 0,
        latitude: Double = 0.0,
        longitude: Double = 0.0,
        replyToMessage: Message? = null
    ) {
        chatRepository.sendMessage(
            chatId, senderId, receiverId, senderName, text, type, mediaUrl, fileName, fileSize, voiceDurationSec, latitude, longitude, replyToMessage
        )
    }

    fun markMessagesAsRead(chatId: String, userId: String) {
        chatRepository.markMessagesAsRead(chatId, userId)
    }

    fun setTyping(chatId: String, isTyping: Boolean) {
        chatRepository.setTyping(chatId, isTyping)
    }

    fun deleteChat(chatId: String) {
        chatRepository.deleteChat(chatId)
    }

    fun deleteMessage(chatId: String, messageId: String) {
        chatRepository.deleteMessage(chatId, messageId)
    }

    fun editMessage(chatId: String, messageId: String, newText: String) {
        chatRepository.editMessage(chatId, messageId, newText)
    }

    fun forwardMessage(targetChatId: String, originalMsg: Message, senderId: String, receiverId: String) {
        chatRepository.forwardMessage(targetChatId, originalMsg, senderId, receiverId)
    }

    fun startOrGetChat(
        currentUserId: String,
        currentUserName: String = "Saya",
        targetUserId: String,
        targetUserName: String
    ): Chat {
        if (currentUserId.isNotBlank() && currentUserId != "current_user") {
            chatRepository.startListeningToUserChats(currentUserId)
        }
        return chatRepository.getOrCreateChatWithUser(
            currentUserId = currentUserId,
            currentUserName = currentUserName,
            otherUserId = targetUserId,
            otherUserName = targetUserName
        )
    }

    // Call operations
    fun startCall(peerName: String, peerAvatar: String, type: CallType) {
        callRepository.startCall(peerName, peerAvatar, type)
    }

    fun endCall() {
        callRepository.endCall()
    }

    fun toggleCallMute() {
        callRepository.toggleMute()
    }

    fun toggleCallSpeaker() {
        callRepository.toggleSpeaker()
    }

    fun toggleCallCamera() {
        callRepository.toggleCamera()
    }

    fun deleteCallLog(logId: String) {
        callRepository.deleteCallLog(logId)
    }

    fun clearCallHistory() {
        callRepository.clearHistory()
    }
}
