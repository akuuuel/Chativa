package com.chativa.data.repository

import com.chativa.data.model.Chat
import com.chativa.data.model.Message
import com.chativa.data.model.MessageStatus
import com.chativa.data.model.MessageType
import com.chativa.data.remote.SupabaseConfig
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class ChatRepository {

    private val repositoryScope = CoroutineScope(Dispatchers.IO + Job())

    private val _chats = MutableStateFlow<List<Chat>>(emptyList())
    val chats: StateFlow<List<Chat>> = _chats

    private val _messagesMap = MutableStateFlow<Map<String, List<Message>>>(emptyMap())
    val messagesMap: StateFlow<Map<String, List<Message>>> = _messagesMap

    private val _typingStatusMap = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val typingStatusMap: StateFlow<Map<String, Boolean>> = _typingStatusMap

    private val activeRealtimeChannels = mutableMapOf<String, Any>()

    init {
        seedInitialMockData()
    }

    private fun isSupabaseConfigured(): Boolean {
        return SupabaseConfig.SUPABASE_URL.startsWith("https://") &&
                !SupabaseConfig.SUPABASE_URL.contains("your-supabase-project")
    }

    private fun seedInitialMockData() {
        val now = System.currentTimeMillis()
        val mockChat1 = Chat(
            id = "chat_1",
            participantIds = listOf("current_user", "contact_1"),
            participantNames = mapOf("contact_1" to "Andi Wijaya", "current_user" to "Saya"),
            participantAvatars = mapOf("contact_1" to "", "current_user" to ""),
            lastMessage = "Halo, apakah proyek CHATIVA sudah siap?",
            lastMessageTime = now - 5 * 60 * 1000,
            unreadCounts = mapOf("current_user" to 2)
        )

        val mockChat2 = Chat(
            id = "chat_2",
            participantIds = listOf("current_user", "contact_2"),
            participantNames = mapOf("contact_2" to "Siti Rahma", "current_user" to "Saya"),
            participantAvatars = mapOf("contact_2" to "", "current_user" to ""),
            lastMessage = "Terima kasih infonya! 👍",
            lastMessageTime = now - 60 * 60 * 1000,
            unreadCounts = mapOf("current_user" to 0)
        )

        val mockChat3 = Chat(
            id = "chat_3",
            participantIds = listOf("current_user", "contact_3"),
            participantNames = mapOf("contact_3" to "Budi Santoso", "current_user" to "Saya"),
            participantAvatars = mapOf("contact_3" to "", "current_user" to ""),
            lastMessage = "Kirim lokasi tempat kumpul ya.",
            lastMessageTime = now - 3 * 3600 * 1000,
            unreadCounts = mapOf("current_user" to 0)
        )

        _chats.value = listOf(mockChat1, mockChat2, mockChat3)

        val msgsChat1 = listOf(
            Message(
                id = "m1",
                chatId = "chat_1",
                senderId = "contact_1",
                receiverId = "current_user",
                senderName = "Andi Wijaya",
                text = "Halo! Selamat datang di CHATIVA 👋",
                timestamp = now - 30 * 60 * 1000,
                status = MessageStatus.READ
            ),
            Message(
                id = "m2",
                chatId = "chat_1",
                senderId = "current_user",
                receiverId = "contact_1",
                senderName = "Saya",
                text = "Halo Andi, terima kasih! Tampilan aplikasinya sangat modern.",
                timestamp = now - 20 * 60 * 1000,
                status = MessageStatus.READ
            ),
            Message(
                id = "m3",
                chatId = "chat_1",
                senderId = "contact_1",
                receiverId = "current_user",
                senderName = "Andi Wijaya",
                text = "Halo, apakah proyek CHATIVA sudah siap?",
                timestamp = now - 5 * 60 * 1000,
                status = MessageStatus.DELIVERED
            )
        )

        val msgsChat2 = listOf(
            Message(
                id = "m4",
                chatId = "chat_2",
                senderId = "contact_2",
                receiverId = "current_user",
                senderName = "Siti Rahma",
                text = "Bisa tolong kirimkan berkas desain terbarunya?",
                timestamp = now - 2 * 3600 * 1000,
                status = MessageStatus.READ
            ),
            Message(
                id = "m5",
                chatId = "chat_2",
                senderId = "current_user",
                receiverId = "contact_2",
                senderName = "Saya",
                text = "Sudah saya kirim lewat email ya.",
                timestamp = now - 70 * 60 * 1000,
                status = MessageStatus.READ
            ),
            Message(
                id = "m6",
                chatId = "chat_2",
                senderId = "contact_2",
                receiverId = "current_user",
                senderName = "Siti Rahma",
                text = "Terima kasih infonya! 👍",
                timestamp = now - 60 * 60 * 1000,
                status = MessageStatus.READ
            )
        )

        _messagesMap.value = mapOf(
            "chat_1" to msgsChat1,
            "chat_2" to msgsChat2,
            "chat_3" to emptyList()
        )
    }

    fun startListeningToUserChats(userId: String) {
        if (userId.isBlank() || userId == "current_user") return
        if (isSupabaseConfigured()) {
            repositoryScope.launch {
                try {
                    val fetchedChats = SupabaseConfig.postgrest["chats"]
                        .select()
                        .decodeList<Chat>()
                        .filter { it.participantIds.contains(userId) }
                        .sortedByDescending { it.lastMessageTime }

                    if (fetchedChats.isNotEmpty()) {
                        val currentList = _chats.value.toMutableList()
                        fetchedChats.forEach { fetched ->
                            val index = currentList.indexOfFirst { it.id == fetched.id }
                            if (index >= 0) {
                                currentList[index] = fetched
                            } else {
                                currentList.add(0, fetched)
                            }
                            startListeningToMessages(fetched.id)
                        }
                        _chats.value = currentList.sortedByDescending { it.lastMessageTime }
                    }

                    // Subscribe to Realtime Postgres Changes
                    val channelKey = "chats_$userId"
                    if (!activeRealtimeChannels.containsKey(channelKey)) {
                        val channel = SupabaseConfig.realtime.channel(channelKey)
                        val changeFlow = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
                            table = "chats"
                        }
                        changeFlow.onEach {
                            fetchUserChatsOnce(userId)
                        }.launchIn(repositoryScope)
                        channel.subscribe()
                        activeRealtimeChannels[channelKey] = channel
                    }
                } catch (_: Exception) {}
            }
        }
    }

    private suspend fun fetchUserChatsOnce(userId: String) {
        if (!isSupabaseConfigured()) return
        try {
            val fetchedChats = SupabaseConfig.postgrest["chats"]
                .select()
                .decodeList<Chat>()
                .filter { it.participantIds.contains(userId) }
                .sortedByDescending { it.lastMessageTime }

            if (fetchedChats.isNotEmpty()) {
                val currentList = _chats.value.toMutableList()
                fetchedChats.forEach { fetched ->
                    val index = currentList.indexOfFirst { it.id == fetched.id }
                    if (index >= 0) {
                        currentList[index] = fetched
                    } else {
                        currentList.add(0, fetched)
                    }
                    startListeningToMessages(fetched.id)
                }
                _chats.value = currentList.sortedByDescending { it.lastMessageTime }
            }
        } catch (_: Exception) {}
    }

    fun startListeningToMessages(chatId: String) {
        if (chatId.isBlank()) return
        if (isSupabaseConfigured()) {
            repositoryScope.launch {
                try {
                    val fetchedMessages = SupabaseConfig.postgrest["messages"]
                        .select { filter { eq("chat_id", chatId) } }
                        .decodeList<Message>()
                        .sortedBy { it.timestamp }

                    if (fetchedMessages.isNotEmpty()) {
                        val newMap = _messagesMap.value.toMutableMap()
                        newMap[chatId] = fetchedMessages
                        _messagesMap.value = newMap
                    }

                    val channelKey = "messages_$chatId"
                    if (!activeRealtimeChannels.containsKey(channelKey)) {
                        val channel = SupabaseConfig.realtime.channel(channelKey)
                        val changeFlow = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
                            table = "messages"
                        }
                        changeFlow.onEach {
                            fetchMessagesOnce(chatId)
                        }.launchIn(repositoryScope)
                        channel.subscribe()
                        activeRealtimeChannels[channelKey] = channel
                    }
                } catch (_: Exception) {}
            }
        }
    }

    private suspend fun fetchMessagesOnce(chatId: String) {
        if (!isSupabaseConfigured()) return
        try {
            val fetchedMessages = SupabaseConfig.postgrest["messages"]
                .select { filter { eq("chat_id", chatId) } }
                .decodeList<Message>()
                .sortedBy { it.timestamp }

            if (fetchedMessages.isNotEmpty()) {
                val newMap = _messagesMap.value.toMutableMap()
                newMap[chatId] = fetchedMessages
                _messagesMap.value = newMap
            }
        } catch (_: Exception) {}
    }

    fun getMessagesForChat(chatId: String): List<Message> {
        startListeningToMessages(chatId)
        return _messagesMap.value[chatId] ?: emptyList()
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
        val now = System.currentTimeMillis()
        val newId = "msg_${now}_${(100..999).random()}"

        val message = Message(
            id = newId,
            chatId = chatId,
            senderId = senderId,
            receiverId = receiverId,
            senderName = senderName,
            text = text,
            type = type,
            mediaUrl = mediaUrl,
            fileName = fileName,
            fileSize = fileSize,
            voiceDurationSec = voiceDurationSec,
            latitude = latitude,
            longitude = longitude,
            timestamp = now,
            status = MessageStatus.SENT,
            replyToId = replyToMessage?.id,
            replyToText = replyToMessage?.text,
            replyToSender = replyToMessage?.senderName
        )

        // Local state update
        val currentMsgs = _messagesMap.value[chatId]?.toMutableList() ?: mutableListOf()
        currentMsgs.add(message)
        val newMap = _messagesMap.value.toMutableMap()
        newMap[chatId] = currentMsgs
        _messagesMap.value = newMap

        val lastTextSummary = when (type) {
            MessageType.TEXT -> text
            MessageType.IMAGE -> "📷 Foto"
            MessageType.VIDEO -> "🎥 Video"
            MessageType.DOCUMENT -> "📄 Dokumen: $fileName"
            MessageType.VOICE -> "🎤 Pesan Suara (${voiceDurationSec}s)"
            MessageType.LOCATION -> "📍 Lokasi"
        }

        val updatedChats = _chats.value.map { chat ->
            if (chat.id == chatId) {
                chat.copy(
                    lastMessage = lastTextSummary,
                    lastMessageTime = now
                )
            } else chat
        }
        _chats.value = updatedChats

        if (isSupabaseConfigured()) {
            repositoryScope.launch {
                try {
                    SupabaseConfig.postgrest["messages"].insert(message)

                    val updatedChat = Chat(
                        id = chatId,
                        participantIds = listOf(senderId, receiverId),
                        lastMessage = lastTextSummary,
                        lastMessageTime = now
                    )
                    SupabaseConfig.postgrest["chats"].upsert(updatedChat)
                } catch (_: Exception) {}
            }
        }
    }

    fun markMessagesAsRead(chatId: String, userId: String) {
        val currentMsgs = _messagesMap.value[chatId] ?: return
        val updated = currentMsgs.map { msg ->
            if (msg.senderId != userId && msg.status != MessageStatus.READ) {
                msg.copy(status = MessageStatus.READ)
            } else msg
        }
        val newMap = _messagesMap.value.toMutableMap()
        newMap[chatId] = updated
        _messagesMap.value = newMap

        _chats.value = _chats.value.map { c ->
            if (c.id == chatId) {
                val newUnreads = c.unreadCounts.toMutableMap()
                newUnreads[userId] = 0
                c.copy(unreadCounts = newUnreads)
            } else c
        }
    }

    fun setTyping(chatId: String, isTyping: Boolean) {
        val newTypingMap = _typingStatusMap.value.toMutableMap()
        newTypingMap[chatId] = isTyping
        _typingStatusMap.value = newTypingMap
    }

    fun deleteChat(chatId: String) {
        _chats.value = _chats.value.filter { it.id != chatId }
        val newMsgMap = _messagesMap.value.toMutableMap()
        newMsgMap.remove(chatId)
        _messagesMap.value = newMsgMap

        if (isSupabaseConfigured()) {
            repositoryScope.launch {
                try {
                    SupabaseConfig.postgrest["chats"].delete { filter { eq("id", chatId) } }
                } catch (_: Exception) {}
            }
        }
    }

    fun deleteMessage(chatId: String, messageId: String) {
        val currentMsgs = _messagesMap.value[chatId]?.toMutableList() ?: return
        currentMsgs.removeAll { it.id == messageId }
        val newMap = _messagesMap.value.toMutableMap()
        newMap[chatId] = currentMsgs
        _messagesMap.value = newMap

        if (isSupabaseConfigured()) {
            repositoryScope.launch {
                try {
                    SupabaseConfig.postgrest["messages"].delete { filter { eq("id", messageId) } }
                } catch (_: Exception) {}
            }
        }
    }

    fun editMessage(chatId: String, messageId: String, newText: String) {
        val currentMsgs = _messagesMap.value[chatId] ?: return
        val updated = currentMsgs.map { msg ->
            if (msg.id == messageId) {
                msg.copy(text = newText, isEdited = true)
            } else msg
        }
        val newMap = _messagesMap.value.toMutableMap()
        newMap[chatId] = updated
        _messagesMap.value = newMap

        if (isSupabaseConfigured()) {
            repositoryScope.launch {
                try {
                    SupabaseConfig.postgrest["messages"].update(mapOf("text" to newText, "is_edited" to true)) {
                        filter { eq("id", messageId) }
                    }
                } catch (_: Exception) {}
            }
        }
    }

    fun forwardMessage(targetChatId: String, originalMsg: Message, senderId: String, receiverId: String) {
        sendMessage(
            chatId = targetChatId,
            senderId = senderId,
            receiverId = receiverId,
            text = originalMsg.text,
            type = originalMsg.type,
            mediaUrl = originalMsg.mediaUrl,
            fileName = originalMsg.fileName,
            fileSize = originalMsg.fileSize,
            voiceDurationSec = originalMsg.voiceDurationSec,
            latitude = originalMsg.latitude,
            longitude = originalMsg.longitude
        )
    }

    fun getOrCreateChatWithUser(
        currentUserId: String,
        currentUserName: String = "Saya",
        otherUserId: String,
        otherUserName: String
    ): Chat {
        val sortedIds = listOf(currentUserId, otherUserId).sorted()
        val deterministicChatId = "chat_${sortedIds[0]}_${sortedIds[1]}"

        val existing = _chats.value.find { chat ->
            chat.id == deterministicChatId || (chat.participantIds.contains(currentUserId) && chat.participantIds.contains(otherUserId))
        }
        if (existing != null) {
            startListeningToMessages(existing.id)
            return existing
        }

        val newChat = Chat(
            id = deterministicChatId,
            participantIds = listOf(currentUserId, otherUserId),
            participantNames = mapOf(currentUserId to currentUserName, otherUserId to otherUserName),
            participantAvatars = mapOf(currentUserId to "", otherUserId to ""),
            lastMessage = "Percakapan baru dibuat",
            lastMessageTime = System.currentTimeMillis()
        )

        _chats.value = listOf(newChat) + _chats.value.filter { it.id != deterministicChatId }
        val newMap = _messagesMap.value.toMutableMap()
        if (!newMap.containsKey(deterministicChatId)) {
            newMap[deterministicChatId] = emptyList()
        }
        _messagesMap.value = newMap

        startListeningToMessages(deterministicChatId)

        if (isSupabaseConfigured()) {
            repositoryScope.launch {
                try {
                    SupabaseConfig.postgrest["chats"].upsert(newChat)
                } catch (_: Exception) {}
            }
        }

        return newChat
    }
}
