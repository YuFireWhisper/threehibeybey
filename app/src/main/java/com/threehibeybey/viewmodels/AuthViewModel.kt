package com.threehibeybey.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.threehibeybey.repositories.AuthRepository
import com.threehibeybey.utils.ValidationUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object LoginSuccess : AuthState()
    object RegisterSuccess : AuthState()
    object EmailVerificationSent : AuthState()
    object PasswordResetEmailSent : AuthState()
    object EmailChangeEmailSent : AuthState()
    object DeleteAccountEmailSent : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _user: MutableStateFlow<FirebaseUser?> = MutableStateFlow(null)
    val user: StateFlow<FirebaseUser?> = _user

    private val _authState: MutableStateFlow<AuthState> = MutableStateFlow(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    init {
        // 檢查當前使用者是否已登入
        val currentUser = authRepository.getCurrentUser()
        if (currentUser != null) {
            _user.value = currentUser
            _authState.value = AuthState.Idle
        }
    }

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

    fun register(email: String, password: String) {
        if (!ValidationUtils.isValidEmail(email)) {
            _authState.value = AuthState.Error("無效的電子郵件格式。")
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

    fun logout() {
        authRepository.logout()
        _user.value = null
        _authState.value = AuthState.Idle
    }

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

    fun sendDeleteAccountEmail(password: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            authRepository.sendDeleteAccountEmail(password).collect { result ->
                when (result) {
                    is AuthRepository.AuthResult.DeleteAccountEmailSent -> {
                        _authState.value = AuthState.DeleteAccountEmailSent
                    }
                    is AuthRepository.AuthResult.Error -> {
                        _authState.value = AuthState.Error(result.message)
                    }
                    else -> {}
                }
            }
        }
    }

    // 新增此方法以重置 AuthState
    fun resetAuthState() {
        _authState.value = AuthState.Idle
    }
}
