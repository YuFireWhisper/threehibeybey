package com.threehibeybey.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.threehibeybey.repositories.AuthRepository
import com.threehibeybey.utils.ValidationUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Represents the authentication state.
 */
sealed class AuthState {
    data object Idle : AuthState()
    data object Loading : AuthState()
    data object LoginSuccess : AuthState()
    data object RegisterSuccess : AuthState()
    data object UpdateEmailSuccess : AuthState()
    data object UpdatePasswordSuccess : AuthState()
    data object DeleteAccountSuccess : AuthState()
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

    init {
        // Check if there is a logged-in user
        val currentUser = authRepository.getCurrentUser()
        if (currentUser != null) {
            _user.value = currentUser
            _authState.value = AuthState.Idle
        }
    }

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
                        _authState.value = AuthState.LoginSuccess
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
                        _authState.value = AuthState.RegisterSuccess
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
     * Updates the user's email after re-authentication.
     *
     * @param newEmail The new email to set.
     * @param currentPassword The current password for re-authentication.
     */
    fun updateEmail(newEmail: String, currentPassword: String) {
        if (!ValidationUtils.isValidEmail(newEmail)) {
            _authState.value = AuthState.Error("無效的電子郵件格式。")
            return
        }

        _authState.value = AuthState.Loading
        viewModelScope.launch {
            authRepository.updateEmail(newEmail, currentPassword).collect { result ->
                when (result) {
                    is AuthRepository.AuthResult.Success -> {
                        _authState.value = AuthState.UpdateEmailSuccess
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
                        _authState.value = AuthState.UpdatePasswordSuccess
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
                        _authState.value = AuthState.DeleteAccountSuccess
                    }
                    is AuthRepository.AuthResult.Error -> {
                        _authState.value = AuthState.Error(result.message)
                    }
                }
            }
        }
    }
}
