package com.threehibeybey.repositories

import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Repository for handling authentication-related operations.
 */
class AuthRepository(private val firebaseAuth: FirebaseAuth) {

    /**
     * Retrieves the current logged-in user.
     */
    fun getCurrentUser(): FirebaseUser? {
        return firebaseAuth.currentUser
    }

    /**
     * Attempts to log in the user with the provided email and password.
     */
    fun login(email: String, password: String): Flow<AuthResult> = callbackFlow {
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user: FirebaseUser? = firebaseAuth.currentUser
                    trySend(AuthResult.Success(user))
                } else {
                    trySend(AuthResult.Error(task.exception?.localizedMessage ?: "登入失敗。"))
                }
            }
        awaitClose { }
    }

    /**
     * Attempts to register a new user with the provided email and password.
     */
    fun register(email: String, password: String): Flow<AuthResult> = callbackFlow {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user: FirebaseUser? = firebaseAuth.currentUser
                    trySend(AuthResult.Success(user))
                } else {
                    trySend(AuthResult.Error(task.exception?.localizedMessage ?: "註冊失敗。"))
                }
            }
        awaitClose { }
    }

    /**
     * Logs out the current user.
     */
    fun logout() {
        firebaseAuth.signOut()
    }

    /**
     * Updates the user's email after re-authentication.
     *
     * @param newEmail The new email to set.
     * @param currentPassword The current password for re-authentication.
     */
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
                                trySend(AuthResult.Error(task.exception?.localizedMessage ?: "更新電子郵件失敗。"))
                            }
                        }
                } else {
                    trySend(AuthResult.Error(reAuthTask.exception?.localizedMessage ?: "重新驗證失敗。"))
                }
            }
        } else {
            trySend(AuthResult.Error("使用者未登入。"))
        }
        awaitClose { }
    }

    /**
     * Updates the user's password after re-authentication.
     */
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
                                trySend(AuthResult.Error(task.exception?.localizedMessage ?: "更新密碼失敗。"))
                            }
                        }
                } else {
                    trySend(AuthResult.Error(reAuthTask.exception?.localizedMessage ?: "重新驗證失敗。"))
                }
            }
        } else {
            trySend(AuthResult.Error("使用者未登入。"))
        }
        awaitClose { }
    }

    /**
     * Deletes the user's account after re-authentication.
     */
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
                                trySend(AuthResult.Error(task.exception?.localizedMessage ?: "刪除帳號失敗。"))
                            }
                        }
                } else {
                    trySend(AuthResult.Error(reAuthTask.exception?.localizedMessage ?: "重新驗證失敗。"))
                }
            }
        } else {
            trySend(AuthResult.Error("使用者未登入。"))
        }
        awaitClose { }
    }

    /**
     * Represents the result of an authentication operation.
     */
    sealed class AuthResult {
        data class Success(val user: FirebaseUser?) : AuthResult()
        data class Error(val message: String) : AuthResult()
    }
}
