package com.chativa.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Chat(
    val id: String = "",
    @SerialName("participant_ids")
    val participantIds: List<String> = emptyList(),
    @SerialName("participant_names")
    val participantNames: Map<String, String> = emptyMap(),
    @SerialName("participant_avatars")
    val participantAvatars: Map<String, String> = emptyMap(),
    @SerialName("last_message")
    val lastMessage: String = "",
    @SerialName("last_message_time")
    val lastMessageTime: Long = System.currentTimeMillis(),
    @SerialName("unread_counts")
    val unreadCounts: Map<String, Int> = emptyMap(),
    @SerialName("typing_users")
    val typingUsers: List<String> = emptyList()
)
