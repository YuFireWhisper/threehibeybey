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
                    if (user != null && user.isEmailVerified) {
                        trySend(AuthResult.Success(user))
                    } else {
                        trySend(AuthResult.Error("您的電子郵件尚未驗證，請檢查您的收件箱。"))
                    }
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
                    user?.sendEmailVerification()?.addOnCompleteListener { verificationTask ->
                        if (verificationTask.isSuccessful) {
                            trySend(AuthResult.EmailVerificationSent)
                        } else {
                            val errorMessage = verificationTask.exception?.let { getErrorMessage(it) } ?: "無法發送驗證郵件。"
                            trySend(AuthResult.Error(errorMessage))
                        }
                    }
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

    fun sendPasswordResetEmail(email: String): Flow<AuthResult> = callbackFlow {
        firebaseAuth.currentUser?.let { currentUser ->
            if (currentUser.email == email) {
                firebaseAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            trySend(AuthResult.PasswordResetEmailSent)
                        } else {
                            val errorMessage = task.exception?.let { getErrorMessage(it) } ?: "無法發送密碼重置郵件。"
                            trySend(AuthResult.Error(errorMessage))
                        }
                    }
            } else {
                trySend(AuthResult.Error("輸入的電子郵件與當前使用者不符。"))
            }
        } ?: run {
            trySend(AuthResult.Error("未登入，無法變更密碼。"))
        }
        awaitClose { }
    }

    fun sendEmailChangeEmail(): Flow<AuthResult> = callbackFlow {
        firebaseAuth.currentUser?.let { currentUser ->
            currentUser.verifyBeforeUpdateEmail(currentUser.email!!)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        trySend(AuthResult.EmailChangeEmailSent)
                    } else {
                        val errorMessage = task.exception?.let { getErrorMessage(it) } ?: "無法發送電子郵件變更確認。"
                        trySend(AuthResult.Error(errorMessage))
                    }
                }
        } ?: run {
            trySend(AuthResult.Error("未登入，無法變更電子郵件。"))
        }
        awaitClose { }
    }

    fun sendDeleteAccountEmail(password: String): Flow<AuthResult> = callbackFlow {
        firebaseAuth.currentUser?.let { currentUser ->
            val credential = EmailAuthProvider.getCredential(currentUser.email!!, password)
            currentUser.reauthenticate(credential)
                .addOnCompleteListener { authTask ->
                    if (authTask.isSuccessful) {
                        currentUser.sendEmailVerification()
                            .addOnCompleteListener { emailTask ->
                                if (emailTask.isSuccessful) {
                                    trySend(AuthResult.DeleteAccountEmailSent)
                                } else {
                                    val errorMessage = emailTask.exception?.let { getErrorMessage(it) } ?: "無法發送帳號刪除確認。"
                                    trySend(AuthResult.Error(errorMessage))
                                }
                            }
                    } else {
                        val errorMessage = authTask.exception?.let { getErrorMessage(it) } ?: "密碼驗證失敗。"
                        trySend(AuthResult.Error(errorMessage))
                    }
                }
        } ?: run {
            trySend(AuthResult.Error("未登入，無法刪除帳號。"))
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
        data object EmailVerificationSent : AuthResult()
        data object PasswordResetEmailSent : AuthResult()
        data object EmailChangeEmailSent : AuthResult()
        data object DeleteAccountEmailSent : AuthResult()
    }
}
