package com.chativa.data.repository

import com.chativa.data.model.CallLog
import com.chativa.data.model.CallStatus
import com.chativa.data.model.CallType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class ActiveCallState(
    val callId: String = "",
    val callerName: String = "",
    val callerAvatar: String = "",
    val receiverName: String = "",
    val receiverAvatar: String = "",
    val type: CallType = CallType.VOICE,
    val isConnected: Boolean = false,
    val isMuted: Boolean = false,
    val isSpeakerOn: Boolean = false,
    val isCameraOn: Boolean = true,
    val durationSec: Int = 0
)

class CallRepository {

    private val _callLogs = MutableStateFlow<List<CallLog>>(emptyList())
    val callLogs: StateFlow<List<CallLog>> = _callLogs

    private val _activeCall = MutableStateFlow<ActiveCallState?>(null)
    val activeCall: StateFlow<ActiveCallState?> = _activeCall

    init {
        seedMockCallLogs()
    }

    private fun seedMockCallLogs() {
        val now = System.currentTimeMillis()
        val log1 = CallLog(
            id = "log_1",
            callerId = "contact_1",
            callerName = "Andi Wijaya",
            callerAvatar = "",
            receiverId = "current_user",
            receiverName = "Saya",
            receiverAvatar = "",
            type = CallType.VOICE,
            status = CallStatus.INCOMING,
            durationSec = 145,
            timestamp = now - 2 * 3600 * 1000
        )

        val log2 = CallLog(
            id = "log_2",
            callerId = "current_user",
            callerName = "Saya",
            callerAvatar = "",
            receiverId = "contact_2",
            receiverName = "Siti Rahma",
            receiverAvatar = "",
            type = CallType.VIDEO,
            status = CallStatus.OUTGOING,
            durationSec = 310,
            timestamp = now - 5 * 3600 * 1000
        )

        val log3 = CallLog(
            id = "log_3",
            callerId = "contact_3",
            callerName = "Budi Santoso",
            callerAvatar = "",
            receiverId = "current_user",
            receiverName = "Saya",
            receiverAvatar = "",
            type = CallType.VOICE,
            status = CallStatus.MISSED,
            durationSec = 0,
            timestamp = now - 24 * 3600 * 1000
        )

        _callLogs.value = listOf(log1, log2, log3)
    }

    fun startCall(peerName: String, peerAvatar: String, type: CallType) {
        val callId = "call_${System.currentTimeMillis()}"
        _activeCall.value = ActiveCallState(
            callId = callId,
            callerName = "Saya",
            receiverName = peerName,
            receiverAvatar = peerAvatar,
            type = type,
            isConnected = true
        )

        // Log call history
        val newLog = CallLog(
            id = callId,
            callerId = "current_user",
            callerName = "Saya",
            receiverId = "peer_user",
            receiverName = peerName,
            type = type,
            status = CallStatus.OUTGOING,
            durationSec = 0,
            timestamp = System.currentTimeMillis()
        )
        _callLogs.value = listOf(newLog) + _callLogs.value
    }

    fun toggleMute() {
        _activeCall.value?.let { call ->
            _activeCall.value = call.copy(isMuted = !call.isMuted)
        }
    }

    fun toggleSpeaker() {
        _activeCall.value?.let { call ->
            _activeCall.value = call.copy(isSpeakerOn = !call.isSpeakerOn)
        }
    }

    fun toggleCamera() {
        _activeCall.value?.let { call ->
            _activeCall.value = call.copy(isCameraOn = !call.isCameraOn)
        }
    }

    fun updateDuration(durationSec: Int) {
        _activeCall.value?.let { call ->
            _activeCall.value = call.copy(durationSec = durationSec)
        }
    }

    fun endCall() {
        _activeCall.value = null
    }

    fun deleteCallLog(logId: String) {
        _callLogs.value = _callLogs.value.filter { it.id != logId }
    }

    fun clearHistory() {
        _callLogs.value = emptyList()
    }
}
