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
 * Represents the state of authentication operations.
 */
sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object LoginSuccess : AuthState()
    object RegisterSuccess : AuthState()
    object EmailVerificationSent : AuthState()
    object PasswordResetEmailSent : AuthState()
    object EmailChangeEmailSent : AuthState()
    object DeleteAccountEmailSent : AuthState()
    data object AccountDeleted : AuthState()
    data class Error(val message: String) : AuthState()
}

/**
 * ViewModel handling authentication-related operations.
 *
 * @param authRepository The repository for authentication operations.
 */
class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _user: MutableStateFlow<FirebaseUser?> = MutableStateFlow(null)
    val user: StateFlow<FirebaseUser?> = _user

    private val _authState: MutableStateFlow<AuthState> = MutableStateFlow(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    init {
        // Check if the current user is logged in
        val currentUser = authRepository.getCurrentUser()
        if (currentUser != null) {
            _user.value = currentUser
            _authState.value = AuthState.Idle
        }
    }

    fun deleteAccount(password: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            authRepository.deleteAccount(password).collect { result ->
                when (result) {
                    is AuthRepository.AuthResult.AccountDeleted -> {
                        _user.value = null
                        _authState.value = AuthState.AccountDeleted
                        logout() // 確保用戶被登出
                    }
                    is AuthRepository.AuthResult.Error -> {
                        _authState.value = AuthState.Error(result.message)
                    }
                    else -> {}
                }
            }
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
                    else -> {}
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
            _authState.value = AuthState.Error("密碼必須至少 8 個字元，包含一個大寫字母和一個數字。")
            return
        }

        _authState.value = AuthState.Loading
        viewModelScope.launch {
            authRepository.register(email, password).collect { result ->
                when (result) {
                    is AuthRepository.AuthResult.EmailVerificationSent -> {
                        _authState.value = AuthState.EmailVerificationSent
                    }
                    is AuthRepository.AuthResult.Error -> {
                        _authState.value = AuthState.Error(result.message)
                    }
                    else -> {}
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
     * Sends a password reset email to the user.
     */
    fun sendPasswordResetEmail(email: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            authRepository.sendPasswordResetEmail(email).collect { result ->
                when (result) {
                    is AuthRepository.AuthResult.PasswordResetEmailSent -> {
                        _authState.value = AuthState.PasswordResetEmailSent
                    }
                    is AuthRepository.AuthResult.Error -> {
                        _authState.value = AuthState.Error(result.message)
                    }
                    else -> {}
                }
            }
        }
    }

    /**
     * Sends an email change request to the user.
     */
    fun sendEmailChangeRequest() {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            authRepository.sendEmailChangeEmail().collect { result ->
                when (result) {
                    is AuthRepository.AuthResult.EmailChangeEmailSent -> {
                        _authState.value = AuthState.EmailChangeEmailSent
                    }
                    is AuthRepository.AuthResult.Error -> {
                        _authState.value = AuthState.Error(result.message)
                    }
                    else -> {}
                }
            }
        }
    }

    /**
     * Resets the authentication state to Idle.
     */
    fun resetAuthState() {
        _authState.value = AuthState.Idle
    }
}
