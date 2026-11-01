package com.chativa.data.repository

import com.chativa.data.model.User
import com.chativa.data.remote.SupabaseConfig
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

import kotlinx.serialization.json.buildJsonObject

import kotlinx.serialization.json.put

class AuthRepository {

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    init {
        checkAuthState()
    }

    private fun isSupabaseConfigured(): Boolean {
        return SupabaseConfig.SUPABASE_URL.startsWith("https://") &&
                !SupabaseConfig.SUPABASE_URL.contains("your-supabase-project")
    }

    private fun checkAuthState() {
        if (isSupabaseConfigured()) {
            try {
                val session = SupabaseConfig.auth.currentSessionOrNull()
                val user = SupabaseConfig.auth.currentUserOrNull()
                if (session != null && user != null) {
                    _currentUser.value = User(
                        id = user.id,
                        email = user.email ?: "",
                        name = user.email?.substringBefore("@") ?: "Pengguna CHATIVA",
                        isOnline = true
                    )
                }
            } catch (_: Exception) {}
        }
    }

    private fun parseAuthError(e: Throwable): Exception {
        val msg = e.message ?: ""
        return when {
            msg.contains("over_email_send_rate_limit", ignoreCase = true) ->
                Exception("Harap tunggu sekitar 60 detik sebelum mencoba mendaftar lagi (Batas keamanan email Supabase).")
            msg.contains("User already registered", ignoreCase = true) ->
                Exception("Email ini sudah terdaftar. Silakan masuk atau gunakan email lain.")
            msg.contains("Invalid login credentials", ignoreCase = true) ->
                Exception("Email atau kata sandi tidak valid.")
            msg.contains("Password should be at least", ignoreCase = true) ->
                Exception("Kata sandi terlalu pendek (minimal 6 karakter).")
            else ->
                Exception(msg.substringBefore("URL:").trim().ifBlank { "Gagal melakukan autentikasi di Supabase" })
        }
    }

    suspend fun login(emailStr: String, pass: String): Result<User> {
        return if (isSupabaseConfigured()) {
            try {
                SupabaseConfig.auth.signInWith(Email) {
                    email = emailStr
                    password = pass
                }
                val supabaseUser = SupabaseConfig.auth.currentUserOrNull()
                    ?: throw Exception("Gagal mendapatkan data sesi Supabase")

                val loggedInUser = User(
                    id = supabaseUser.id,
                    email = supabaseUser.email ?: emailStr,
                    name = supabaseUser.email?.substringBefore("@") ?: emailStr.substringBefore("@"),
                    isOnline = true
                )

                try {
                    SupabaseConfig.postgrest["users"].upsert(loggedInUser)
                } catch (_: Exception) {}

                _currentUser.value = loggedInUser
                Result.success(loggedInUser)
            } catch (e: Exception) {
                Result.failure(parseAuthError(e))
            }
        } else {
            val mockUser = User(
                id = "user_demo_1",
                name = emailStr.substringBefore("@").replaceFirstChar { it.uppercase() },
                email = emailStr,
                phoneNumber = "+6281234567890",
                about = "Halo! Saya menggunakan CHATIVA.",
                isOnline = true,
                isEmailVerified = true
            )
            _currentUser.value = mockUser
            Result.success(mockUser)
        }
    }

    suspend fun register(nameStr: String, emailStr: String, pass: String, phoneStr: String): Result<User> {
        return if (isSupabaseConfigured()) {
            try {
                val userInfo = SupabaseConfig.auth.signUpWith(Email) {
                    email = emailStr
                    password = pass
                    data = buildJsonObject {
                        put("name", nameStr)
                        put("phone", phoneStr)
                    }
                }
                val uid = userInfo?.id?.ifEmpty { null }
                    ?: SupabaseConfig.auth.currentUserOrNull()?.id
                    ?: throw Exception("Gagal mendaftarkan akun di Supabase")

                val newUser = User(
                    id = uid,
                    name = nameStr,
                    email = emailStr,
                    phoneNumber = phoneStr,
                    about = "Halo! Saya menggunakan CHATIVA.",
                    isOnline = true,
                    lastSeen = System.currentTimeMillis()
                )

                try {
                    SupabaseConfig.postgrest["users"].upsert(newUser)
                } catch (_: Exception) {}

                _currentUser.value = newUser
                Result.success(newUser)
            } catch (e: Exception) {
                Result.failure(parseAuthError(e))
            }
        } else {
            val newUser = User(
                id = "user_${System.currentTimeMillis()}",
                name = nameStr,
                email = emailStr,
                phoneNumber = phoneStr,
                about = "Halo! Saya menggunakan CHATIVA.",
                isOnline = true,
                isEmailVerified = false
            )
            _currentUser.value = newUser
            Result.success(newUser)
        }
    }

    suspend fun resetPassword(emailStr: String): Result<Unit> {
        return if (isSupabaseConfigured()) {
            try {
                SupabaseConfig.auth.resetPasswordForEmail(emailStr)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        } else {
            Result.success(Unit)
        }
    }

    suspend fun sendEmailVerification(): Result<Unit> {
        return Result.success(Unit)
    }

    suspend fun updateUserProfile(nameStr: String, aboutStr: String, phoneStr: String, profilePicUrlStr: String = ""): Result<User> {
        val current = _currentUser.value ?: return Result.failure(Exception("Tidak ada pengguna aktif"))
        val updated = current.copy(
            name = nameStr,
            about = aboutStr,
            phoneNumber = phoneStr,
            profilePicUrl = if (profilePicUrlStr.isNotEmpty()) profilePicUrlStr else current.profilePicUrl
        )

        if (isSupabaseConfigured()) {
            try {
                SupabaseConfig.postgrest["users"].upsert(updated)
            } catch (_: Exception) {}
        }

        _currentUser.value = updated
        return Result.success(updated)
    }

    suspend fun logout() {
        if (isSupabaseConfigured()) {
            try {
                SupabaseConfig.auth.signOut()
            } catch (_: Exception) {}
        }
        _currentUser.value = null
    }
}
