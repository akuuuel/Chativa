package com.chativa.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chativa.data.model.User
import com.chativa.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    data class Success(val user: User, val message: String = "") : AuthUiState()
    data class Error(val error: String) : AuthUiState()
}

class AuthViewModel(
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    val currentUser: StateFlow<User?> = authRepository.currentUser

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState

    fun login(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            _uiState.value = AuthUiState.Error("Email dan kata sandi tidak boleh kosong")
            return
        }
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val res = authRepository.login(email, pass)
            res.fold(
                onSuccess = { user -> _uiState.value = AuthUiState.Success(user, "Login berhasil") },
                onFailure = { err -> _uiState.value = AuthUiState.Error(err.message ?: "Login gagal") }
            )
        }
    }

    fun register(name: String, email: String, pass: String, confirmPass: String, phone: String) {
        if (name.isBlank() || email.isBlank() || pass.isBlank() || phone.isBlank()) {
            _uiState.value = AuthUiState.Error("Semua kolom harus diisi")
            return
        }
        if (pass != confirmPass) {
            _uiState.value = AuthUiState.Error("Kata sandi dan konfirmasi tidak cocok")
            return
        }
        if (pass.length < 6) {
            _uiState.value = AuthUiState.Error("Kata sandi minimal 6 karakter")
            return
        }
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val res = authRepository.register(name, email, pass, phone)
            res.fold(
                onSuccess = { user -> _uiState.value = AuthUiState.Success(user, "Registrasi berhasil. Silakan verifikasi email Anda.") },
                onFailure = { err -> _uiState.value = AuthUiState.Error(err.message ?: "Registrasi gagal") }
            )
        }
    }

    fun resetPassword(email: String) {
        if (email.isBlank()) {
            _uiState.value = AuthUiState.Error("Email wajib diisi")
            return
        }
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val res = authRepository.resetPassword(email)
            res.fold(
                onSuccess = { _uiState.value = AuthUiState.Success(User(), "Tautan reset kata sandi telah dikirim ke $email") },
                onFailure = { err -> _uiState.value = AuthUiState.Error(err.message ?: "Gagal mengirim email reset") }
            )
        }
    }

    fun sendEmailVerification() {
        viewModelScope.launch {
            val res = authRepository.sendEmailVerification()
            res.fold(
                onSuccess = { _uiState.value = AuthUiState.Success(currentUser.value ?: User(), "Email verifikasi berhasil dikirim!") },
                onFailure = { err -> _uiState.value = AuthUiState.Error(err.message ?: "Gagal mengirim email verifikasi") }
            )
        }
    }

    fun updateProfile(name: String, about: String, phone: String, profilePicUrl: String = "") {
        viewModelScope.launch {
            val res = authRepository.updateUserProfile(name, about, phone, profilePicUrl)
            res.fold(
                onSuccess = { user -> _uiState.value = AuthUiState.Success(user, "Profil berhasil diperbarui") },
                onFailure = { err -> _uiState.value = AuthUiState.Error(err.message ?: "Gagal memperbarui profil") }
            )
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _uiState.value = AuthUiState.Idle
        }
    }

    fun resetState() {
        _uiState.value = AuthUiState.Idle
    }
}
