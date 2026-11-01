package com.chativa.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Contact(
    val id: String = "",
    val name: String = "",
    @SerialName("phone_number")
    val phoneNumber: String = "",
    val email: String = "",
    @SerialName("profile_pic_url")
    val profilePicUrl: String = "",
    @SerialName("registered_user_id")
    val registeredUserId: String? = null,
    @SerialName("is_registered")
    val isRegistered: Boolean = false,
    val about: String = "",
    @SerialName("is_online")
    val isOnline: Boolean = false,
    @SerialName("last_seen")
    val lastSeen: Long = System.currentTimeMillis()
)
