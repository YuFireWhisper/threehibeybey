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
    object UpdateEmailSuccess : AuthState()
    object UpdatePasswordSuccess : AuthState()
    object DeleteAccountSuccess : AuthState()
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

    fun logout() {
        authRepository.logout()
        _user.value = null
        _authState.value = AuthState.Idle
    }

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

    fun updatePassword(newPassword: String, currentPassword: String) {
        if (!ValidationUtils.isPasswordStrong(newPassword)) {
            _authState.value = AuthState.Error("新密碼強度不足，請包含至少8個字元，且包含數字與大寫字母。")
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

    // 新增此方法以重置 AuthState
    fun resetAuthState() {
        _authState.value = AuthState.Idle
    }
}
