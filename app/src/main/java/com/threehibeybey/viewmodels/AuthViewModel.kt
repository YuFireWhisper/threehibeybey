package com.threehibeybey.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.threehibeybey.repositories.AuthRepository
import com.threehibeybey.utils.ValidationUtils
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Represents the authentication state.
 */
sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}

/**
 * ViewModel for handling authentication-related operations.
 */
class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _user: MutableStateFlow<FirebaseUser?> = MutableStateFlow(null)
    val user: StateFlow<FirebaseUser?> = _user

    private val _authState: MutableStateFlow<AuthState> = MutableStateFlow(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    /**
     * Attempts to log in the user with the provided email and password.
     */
    fun login(email: String, password: String) {
        if (!ValidationUtils.isValidEmail(email)) {
            _authState.value = AuthState.Error("無效的電子郵件格式。")
            return
        }

        if (!ValidationUtils.isPasswordStrong(password)) {
            _authState.value = AuthState.Error("密碼強度不足。")
            return
        }

        _authState.value = AuthState.Loading
        viewModelScope.launch {
            authRepository.login(email, password).collect { result ->
                when (result) {
                    is AuthRepository.AuthResult.Success -> {
                        _user.value = result.user
                        _authState.value = AuthState.Success
                    }
                    is AuthRepository.AuthResult.Error -> {
                        _authState.value = AuthState.Error(result.message)
                    }
                }
            }
        }
    }

    /**
     * Attempts to register a new user with the provided email and password.
     */
    fun register(email: String, password: String) {
        if (!ValidationUtils.isValidEmail(email)) {
            _authState.value = AuthState.Error("無效的電子郵件格式。")
            return
        }

        if (!ValidationUtils.isPasswordStrong(password)) {
            _authState.value = AuthState.Error("密碼強度不足。")
            return
        }

        _authState.value = AuthState.Loading
        viewModelScope.launch {
            authRepository.register(email, password).collect { result ->
                when (result) {
                    is AuthRepository.AuthResult.Success -> {
                        _user.value = result.user
                        _authState.value = AuthState.Success
                    }
                    is AuthRepository.AuthResult.Error -> {
                        _authState.value = AuthState.Error(result.message)
                    }
                }
            }
        }
    }

    /**
     * Logs out the current user.
     */
    fun logout() {
        authRepository.logout()
        _user.value = null
        _authState.value = AuthState.Idle
    }

    /**
     * Sends a password reset email.
     */
    fun sendPasswordReset(email: String) {
        if (!ValidationUtils.isValidEmail(email)) {
            _authState.value = AuthState.Error("無效的電子郵件格式。")
            return
        }

        viewModelScope.launch {
            authRepository.sendPasswordReset(email).collect { result ->
                when (result) {
                    is AuthRepository.AuthResult.Success -> {
                        _authState.value = AuthState.Success
                    }
                    is AuthRepository.AuthResult.Error -> {
                        _authState.value = AuthState.Error(result.message)
                    }
                }
            }
        }
    }

    /**
     * Updates the user's email after re-authentication.
     */
    fun updateEmail(newEmail: String, password: String) {
        if (!ValidationUtils.isValidEmail(newEmail)) {
            _authState.value = AuthState.Error("無效的電子郵件格式。")
            return
        }

        _authState.value = AuthState.Loading
        viewModelScope.launch {
            authRepository.updateEmail(newEmail, password).collect { result ->
                when (result) {
                    is AuthRepository.AuthResult.Success -> {
                        _authState.value = AuthState.Success
                    }
                    is AuthRepository.AuthResult.Error -> {
                        _authState.value = AuthState.Error(result.message)
                    }
                }
            }
        }
    }

    /**
     * Updates the user's password after re-authentication.
     */
    fun updatePassword(newPassword: String, currentPassword: String) {
        if (!ValidationUtils.isPasswordStrong(newPassword)) {
            _authState.value = AuthState.Error("新密碼強度不足。")
            return
        }

        _authState.value = AuthState.Loading
        viewModelScope.launch {
            authRepository.updatePassword(newPassword, currentPassword).collect { result ->
                when (result) {
                    is AuthRepository.AuthResult.Success -> {
                        _authState.value = AuthState.Success
                    }
                    is AuthRepository.AuthResult.Error -> {
                        _authState.value = AuthState.Error(result.message)
                    }
                }
            }
        }
    }

    /**
     * Deletes the user's account after re-authentication.
     */
    fun deleteAccount(password: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            authRepository.deleteAccount(password).collect { result ->
                when (result) {
                    is AuthRepository.AuthResult.Success -> {
                        _user.value = null
                        _authState.value = AuthState.Success
                    }
                    is AuthRepository.AuthResult.Error -> {
                        _authState.value = AuthState.Error(result.message)
                    }
                }
            }
        }
    }
}
