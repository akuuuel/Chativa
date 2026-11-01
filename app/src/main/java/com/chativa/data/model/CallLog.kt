package com.chativa.data.model

enum class CallType {
    VOICE,
    VIDEO
}

enum class CallStatus {
    INCOMING,
    OUTGOING,
    MISSED
}

data class CallLog(
    val id: String = "",
    val callerId: String = "",
    val callerName: String = "",
    val callerAvatar: String = "",
    val receiverId: String = "",
    val receiverName: String = "",
    val receiverAvatar: String = "",
    val type: CallType = CallType.VOICE,
    val status: CallStatus = CallStatus.INCOMING,
    val durationSec: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)
