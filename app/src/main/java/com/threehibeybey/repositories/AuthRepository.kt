package com.threehibeybey.repositories

import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class AuthRepository(private val firebaseAuth: FirebaseAuth) {

    fun getCurrentUser(): FirebaseUser? {
        return firebaseAuth.currentUser
    }

    fun login(email: String, password: String): Flow<AuthResult> = callbackFlow {
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user: FirebaseUser? = firebaseAuth.currentUser
                    trySend(AuthResult.Success(user))
                } else {
                    val errorMessage = task.exception?.let { getErrorMessage(it) } ?: "登入失敗。"
                    trySend(AuthResult.Error(errorMessage))
                }
            }
        awaitClose { }
    }

    fun register(email: String, password: String): Flow<AuthResult> = callbackFlow {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user: FirebaseUser? = firebaseAuth.currentUser
                    trySend(AuthResult.Success(user))
                } else {
                    val errorMessage = task.exception?.let { getErrorMessage(it) } ?: "註冊失敗。"
                    trySend(AuthResult.Error(errorMessage))
                }
            }
        awaitClose { }
    }

    fun logout() {
        firebaseAuth.signOut()
    }

    fun updateEmail(newEmail: String, currentPassword: String): Flow<AuthResult> = callbackFlow {
        val user = firebaseAuth.currentUser
        if (user != null) {
            val credential = EmailAuthProvider.getCredential(user.email ?: "", currentPassword)
            user.reauthenticate(credential).addOnCompleteListener { reAuthTask ->
                if (reAuthTask.isSuccessful) {
                    user.updateEmail(newEmail)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                trySend(AuthResult.Success(user))
                            } else {
                                val errorMessage = task.exception?.let { getErrorMessage(it) } ?: "更新電子郵件失敗。"
                                trySend(AuthResult.Error(errorMessage))
                            }
                        }
                } else {
                    val errorMessage = reAuthTask.exception?.let { getErrorMessage(it) } ?: "重新驗證失敗。"
                    trySend(AuthResult.Error(errorMessage))
                }
            }
        } else {
            trySend(AuthResult.Error("使用者未登入。"))
        }
        awaitClose { }
    }

    fun updatePassword(newPassword: String, currentPassword: String): Flow<AuthResult> = callbackFlow {
        val user = firebaseAuth.currentUser
        if (user != null) {
            val credential = EmailAuthProvider.getCredential(user.email ?: "", currentPassword)
            user.reauthenticate(credential).addOnCompleteListener { reAuthTask ->
                if (reAuthTask.isSuccessful) {
                    user.updatePassword(newPassword)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                trySend(AuthResult.Success(user))
                            } else {
                                val errorMessage = task.exception?.let { getErrorMessage(it) } ?: "更新密碼失敗。"
                                trySend(AuthResult.Error(errorMessage))
                            }
                        }
                } else {
                    val errorMessage = reAuthTask.exception?.let { getErrorMessage(it) } ?: "重新驗證失敗。"
                    trySend(AuthResult.Error(errorMessage))
                }
            }
        } else {
            trySend(AuthResult.Error("使用者未登入。"))
        }
        awaitClose { }
    }

    fun deleteAccount(password: String): Flow<AuthResult> = callbackFlow {
        val user = firebaseAuth.currentUser
        if (user != null) {
            val credential = EmailAuthProvider.getCredential(user.email ?: "", password)
            user.reauthenticate(credential).addOnCompleteListener { reAuthTask ->
                if (reAuthTask.isSuccessful) {
                    user.delete()
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                trySend(AuthResult.Success(null))
                            } else {
                                val errorMessage = task.exception?.let { getErrorMessage(it) } ?: "刪除帳號失敗。"
                                trySend(AuthResult.Error(errorMessage))
                            }
                        }
                } else {
                    val errorMessage = reAuthTask.exception?.let { getErrorMessage(it) } ?: "重新驗證失敗。"
                    trySend(AuthResult.Error(errorMessage))
                }
            }
        } else {
            trySend(AuthResult.Error("使用者未登入。"))
        }
        awaitClose { }
    }

    private fun getErrorMessage(exception: Exception): String {
        return if (exception is FirebaseAuthException) {
            when (exception.errorCode) {
                "ERROR_WRONG_PASSWORD" -> "密碼錯誤，請再試一次。"
                "ERROR_USER_NOT_FOUND" -> "找不到使用者，請檢查電子郵件。"
                "ERROR_EMAIL_ALREADY_IN_USE" -> "此電子郵件已被使用。"
                else -> "發生未知錯誤：${exception.localizedMessage}"
            }
        } else {
            "發生錯誤：${exception.localizedMessage}"
        }
    }

    sealed class AuthResult {
        data class Success(val user: FirebaseUser?) : AuthResult()
        data class Error(val message: String) : AuthResult()
    }
}
