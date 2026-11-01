package com.chativa.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

enum class MessageType {
    TEXT,
    IMAGE,
    VIDEO,
    DOCUMENT,
    VOICE,
    LOCATION
}

enum class MessageStatus {
    SENT,       // 1 centang
    DELIVERED,  // 2 centang abu-abu
    READ        // 2 centang biru
}

@Serializable
data class Message(
    val id: String = "",
    @SerialName("chat_id")
    val chatId: String = "",
    @SerialName("sender_id")
    val senderId: String = "",
    @SerialName("receiver_id")
    val receiverId: String = "",
    @SerialName("sender_name")
    val senderName: String = "",
    val text: String = "",
    val type: MessageType = MessageType.TEXT,
    @SerialName("media_url")
    val mediaUrl: String = "",
    @SerialName("file_name")
    val fileName: String = "",
    @SerialName("file_size")
    val fileSize: String = "",
    @SerialName("voice_duration_sec")
    val voiceDurationSec: Int = 0,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis(),
    val status: MessageStatus = MessageStatus.SENT,
    @SerialName("reply_to_id")
    val replyToId: String? = null,
    @SerialName("reply_to_text")
    val replyToText: String? = null,
    @SerialName("reply_to_sender")
    val replyToSender: String? = null,
    @SerialName("is_edited")
    val isEdited: Boolean = false,
    @SerialName("is_forwarded")
    val isForwarded: Boolean = false
)
