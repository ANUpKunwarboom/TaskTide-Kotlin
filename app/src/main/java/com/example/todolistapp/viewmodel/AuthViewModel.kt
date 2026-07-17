package com.example.todolistapp.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todolistapp.model.UserModel
import com.example.todolistapp.repo.AuthRepo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle    : AuthState()
    object Loading : AuthState()
    data class Success(val user: UserModel) : AuthState()
    data class Error(val message: String)   : AuthState()
}

sealed class ProfileState {
    object Idle    : ProfileState()
    object Loading : ProfileState()
    object Success : ProfileState()
    data class Error(val message: String) : ProfileState()
}

/**
 * AuthViewModel coordinates authentication state between the UI and AuthRepo.
 * It exposes state flows for loading, errors, and user data.
 */
class AuthViewModel(private val repo: AuthRepo = AuthRepo()) : ViewModel() {

    private val _authState    = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    private val _profileState = MutableStateFlow<ProfileState>(ProfileState.Idle)
    val profileState: StateFlow<ProfileState> = _profileState

    private val _userData     = MutableStateFlow<UserModel?>(null)
    val userData: StateFlow<UserModel?> = _userData

    val currentUser get() = repo.getCurrentUser()

    // ── Auth ──────────────────────────────────────
    fun register(name: String, email: String, password: String) {
        if (name.isBlank() || email.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("All fields are required"); return
        }
        if (password.length < 6) {
            _authState.value = AuthState.Error("Password must be at least 6 characters"); return
        }
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            repo.register(name, email, password).fold(
                onSuccess = { _authState.value = AuthState.Success(it); _userData.value = it },
                onFailure = { _authState.value = AuthState.Error(it.message ?: "Registration failed") }
            )
        }
    }

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("Email and password are required"); return
        }
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            repo.login(email, password).fold(
                onSuccess = { _authState.value = AuthState.Success(it); _userData.value = it },
                onFailure = { _authState.value = AuthState.Error(it.message ?: "Login failed") }
            )
        }
    }

    fun logout() {
        repo.logout()
        _authState.value = AuthState.Idle
        _userData.value  = null
    }

    fun resetState()        { _authState.value    = AuthState.Idle }
    fun resetProfileState() { _profileState.value = ProfileState.Idle }

    // ── Password Reset ────────────────────────────
    fun sendPasswordReset(email: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            repo.sendPasswordReset(email).fold(
                onSuccess = { onResult(true,  "Reset email sent! Check your inbox.") },
                onFailure = { onResult(false, it.message ?: "Failed to send reset email") }
            )
        }
    }

    // ── Profile ───────────────────────────────────
    fun loadUserData() {
        viewModelScope.launch {
            repo.getUserData().onSuccess { _userData.value = it }
        }
    }

    fun updateName(newName: String) {
        if (newName.isBlank()) {
            _profileState.value = ProfileState.Error("Name cannot be empty"); return
        }
        viewModelScope.launch {
            _profileState.value = ProfileState.Loading
            repo.updateName(newName.trim()).fold(
                onSuccess = {
                    _userData.value = _userData.value?.copy(name = newName.trim())
                    _profileState.value = ProfileState.Success
                },
                onFailure = { _profileState.value = ProfileState.Error(it.message ?: "Failed to update name") }
            )
        }
    }

    fun updateEmail(currentPassword: String, newEmail: String) {
        if (currentPassword.isBlank() || newEmail.isBlank()) {
            _profileState.value = ProfileState.Error("All fields are required"); return
        }
        viewModelScope.launch {
            _profileState.value = ProfileState.Loading
            repo.updateEmail(currentPassword, newEmail.trim()).fold(
                onSuccess = {
                    _userData.value = _userData.value?.copy(email = newEmail.trim())
                    _profileState.value = ProfileState.Success
                },
                onFailure = { _profileState.value = ProfileState.Error(it.message ?: "Failed to update email") }
            )
        }
    }

    fun changePassword(currentPassword: String, newPassword: String, confirmPassword: String) {
        if (currentPassword.isBlank() || newPassword.isBlank()) {
            _profileState.value = ProfileState.Error("All fields are required"); return
        }
        if (newPassword != confirmPassword) {
            _profileState.value = ProfileState.Error("Passwords do not match"); return
        }
        if (newPassword.length < 6) {
            _profileState.value = ProfileState.Error("Password must be at least 6 characters"); return
        }
        viewModelScope.launch {
            _profileState.value = ProfileState.Loading
            repo.changePassword(currentPassword, newPassword).fold(
                onSuccess = { _profileState.value = ProfileState.Success },
                onFailure = { _profileState.value = ProfileState.Error(it.message ?: "Failed to change password") }
            )
        }
    }

    fun uploadAvatar(context: Context, uri: Uri) {
        viewModelScope.launch {
            _profileState.value = ProfileState.Loading
            repo.uploadAvatar(context, uri).fold(
                onSuccess = { dataUrl ->
                    _userData.value = _userData.value?.copy(avatarUrl = dataUrl)
                    _profileState.value = ProfileState.Success
                },
                onFailure = { _profileState.value = ProfileState.Error(it.message ?: "Failed to upload photo") }
            )
        }
    }
}
