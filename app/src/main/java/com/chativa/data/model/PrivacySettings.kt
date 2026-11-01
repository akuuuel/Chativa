package com.chativa.data.model

enum class PrivacyOption {
    EVERYONE,
    MY_CONTACTS,
    NOBODY
}

data class PrivacySettings(
    val whoCanSeeOnline: PrivacyOption = PrivacyOption.EVERYONE,
    val whoCanSeeLastSeen: PrivacyOption = PrivacyOption.EVERYONE,
    val whoCanSeeProfilePic: PrivacyOption = PrivacyOption.EVERYONE,
    val blockedUserIds: List<String> = emptyList(),
    val showReadReceipts: Boolean = true
)
