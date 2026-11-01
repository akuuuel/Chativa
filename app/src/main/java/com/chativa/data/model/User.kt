package com.chativa.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    @SerialName("phone_number")
    val phoneNumber: String = "",
    val about: String = "Halo! Saya menggunakan CHATIVA.",
    @SerialName("profile_pic_url")
    val profilePicUrl: String = "",
    @SerialName("is_online")
    val isOnline: Boolean = false,
    @SerialName("last_seen")
    val lastSeen: Long = System.currentTimeMillis(),
    @SerialName("is_typing_to")
    val isTypingTo: String? = null,
    @SerialName("is_email_verified")
    val isEmailVerified: Boolean = false,
    @SerialName("account_status")
    val accountStatus: String = "Aktif"
)
