package com.chativa.data.repository

import com.chativa.data.model.Contact
import com.chativa.data.model.User
import com.chativa.data.remote.SupabaseConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ContactRepository {

    private val _contacts = MutableStateFlow<List<Contact>>(emptyList())
    val contacts: StateFlow<List<Contact>> = _contacts

    init {
        seedMockContacts()
    }

    private fun isSupabaseConfigured(): Boolean {
        return SupabaseConfig.SUPABASE_URL.startsWith("https://") &&
                !SupabaseConfig.SUPABASE_URL.contains("your-supabase-project")
    }

    suspend fun fetchRegisteredUsersAsContacts(currentUserId: String = "") {
        if (isSupabaseConfigured()) {
            try {
                val supabaseUsers = SupabaseConfig.postgrest["users"]
                    .select()
                    .decodeList<User>()

                if (supabaseUsers.isNotEmpty()) {
                    val currentList = _contacts.value.toMutableList()
                    supabaseUsers.forEach { user ->
                        if (currentUserId.isBlank() || user.id != currentUserId) {
                            val userEmail = user.email
                            val userPhone = user.phoneNumber ?: ""
                            val index = currentList.indexOfFirst {
                                it.registeredUserId == user.id ||
                                        (!it.email.isNullOrBlank() && it.email.equals(userEmail, ignoreCase = true)) ||
                                        (!it.phoneNumber.isNullOrBlank() && it.phoneNumber == userPhone)
                            }
                            if (index >= 0) {
                                val existing = currentList[index]
                                currentList[index] = existing.copy(
                                    registeredUserId = user.id,
                                    isRegistered = true,
                                    isOnline = user.isOnline,
                                    about = user.about?.ifEmpty { existing.about ?: "" } ?: (existing.about ?: "")
                                )
                            } else {
                                currentList.add(
                                    Contact(
                                        id = "contact_${user.id}",
                                        name = user.name.ifEmpty { userEmail.substringBefore("@") },
                                        phoneNumber = user.phoneNumber,
                                        email = user.email,
                                        about = user.about ?: "Ada di CHATIVA",
                                        registeredUserId = user.id,
                                        isRegistered = true,
                                        isOnline = user.isOnline,
                                        lastSeen = user.lastSeen
                                    )
                                )
                            }
                        }
                    }
                    _contacts.value = currentList
                }
            } catch (_: Exception) {}
        }
    }

    private fun seedMockContacts() {
        val now = System.currentTimeMillis()
        val mock1 = Contact(
            id = "c1",
            name = "Andi Wijaya",
            phoneNumber = "+6281234567890",
            email = "andi@chativa.com",
            about = "Ada yang bisa dibantu?",
            registeredUserId = "contact_1",
            isRegistered = true,
            isOnline = true,
            lastSeen = now
        )

        val mock2 = Contact(
            id = "c2",
            name = "Siti Rahma",
            phoneNumber = "+6289876543210",
            email = "siti@chativa.com",
            about = "Sedang sibuk",
            registeredUserId = "contact_2",
            isRegistered = true,
            isOnline = false,
            lastSeen = now - 15 * 60 * 1000
        )

        val mock3 = Contact(
            id = "c3",
            name = "Budi Santoso",
            phoneNumber = "+628555123456",
            email = "budi@chativa.com",
            about = "Hanya pesan singkat",
            registeredUserId = "contact_3",
            isRegistered = true,
            isOnline = true,
            lastSeen = now
        )

        val mock4 = Contact(
            id = "c4",
            name = "Dewi Lestari",
            phoneNumber = "+628777999111",
            email = "dewi@gmail.com",
            about = "Pengguna CHATIVA",
            registeredUserId = "contact_4",
            isRegistered = true,
            isOnline = true,
            lastSeen = now
        )

        _contacts.value = listOf(mock1, mock2, mock3, mock4)
    }

    suspend fun addContact(name: String, phone: String, email: String = ""): Contact {
        var resolvedUserId = "reg_${System.currentTimeMillis()}"
        var about = "Ada di CHATIVA"

        if (isSupabaseConfigured()) {
            try {
                if (email.isNotBlank()) {
                    val matching = SupabaseConfig.postgrest["users"]
                        .select { filter { eq("email", email) } }
                        .decodeList<User>()
                    if (matching.isNotEmpty()) {
                        val first = matching.first()
                        resolvedUserId = first.id
                        about = first.about ?: about
                    }
                }
                if (resolvedUserId.startsWith("reg_") && phone.isNotBlank()) {
                    val matching = SupabaseConfig.postgrest["users"]
                        .select { filter { eq("phone_number", phone) } }
                        .decodeList<User>()
                    if (matching.isNotEmpty()) {
                        val first = matching.first()
                        resolvedUserId = first.id
                        about = first.about ?: about
                    }
                }
            } catch (_: Exception) {}
        }

        val newContact = Contact(
            id = "contact_${System.currentTimeMillis()}",
            name = name,
            phoneNumber = phone,
            email = email,
            about = about,
            registeredUserId = resolvedUserId,
            isRegistered = true,
            isOnline = true,
            lastSeen = System.currentTimeMillis()
        )

        _contacts.value = _contacts.value + newContact
        return newContact
    }

    fun updateContact(contactId: String, newName: String, newPhone: String, newEmail: String) {
        val updatedList = _contacts.value.map { c ->
            if (c.id == contactId) {
                c.copy(name = newName, phoneNumber = newPhone, email = newEmail)
            } else c
        }
        _contacts.value = updatedList
    }

    fun deleteContact(contactId: String) {
        _contacts.value = _contacts.value.filter { it.id != contactId }
    }
}
