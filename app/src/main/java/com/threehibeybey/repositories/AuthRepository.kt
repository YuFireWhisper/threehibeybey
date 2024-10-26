package com.threehibeybey.repositories

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
     * Sends a password reset email to the specified email address.
     */
    fun sendPasswordReset(email: String): Flow<AuthResult> = callbackFlow {
        firebaseAuth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    trySend(AuthResult.Success(null))
                } else {
                    trySend(AuthResult.Error(task.exception?.localizedMessage ?: "發送密碼重設郵件失敗。"))
                }
            }
        awaitClose { }
    }

    /**
     * Updates the user's email after re-authentication.
     */
    fun updateEmail(newEmail: String): Flow<AuthResult> = callbackFlow {
        val user = firebaseAuth.currentUser
        user?.updateEmail(newEmail)
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    trySend(AuthResult.Success(user))
                } else {
                    trySend(AuthResult.Error(task.exception?.localizedMessage ?: "更新電子郵件失敗。"))
                }
            }
        awaitClose { }
    }

    /**
     * Updates the user's password after re-authentication.
     */
    fun updatePassword(newPassword: String, currentPassword: String): Flow<AuthResult> = callbackFlow {
        val user = firebaseAuth.currentUser
        val credential = com.google.firebase.auth.EmailAuthProvider.getCredential(user?.email ?: "", currentPassword)
        user?.reauthenticate(credential)?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                user.updatePassword(newPassword)
                    .addOnCompleteListener { updateTask ->
                        if (updateTask.isSuccessful) {
                            trySend(AuthResult.Success(user))
                        } else {
                            trySend(AuthResult.Error(updateTask.exception?.localizedMessage ?: "更新密碼失敗。"))
                        }
                    }
            } else {
                trySend(AuthResult.Error(task.exception?.localizedMessage ?: "重新驗證失敗。"))
            }
        }
        awaitClose { }
    }

    /**
     * Deletes the user's account after re-authentication.
     */
    fun deleteAccount(password: String): Flow<AuthResult> = callbackFlow {
        val user = firebaseAuth.currentUser
        val credential = com.google.firebase.auth.EmailAuthProvider.getCredential(user?.email ?: "", password)
        user?.reauthenticate(credential)?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                user.delete()
                    .addOnCompleteListener { deleteTask ->
                        if (deleteTask.isSuccessful) {
                            trySend(AuthResult.Success(null))
                        } else {
                            trySend(AuthResult.Error(deleteTask.exception?.localizedMessage ?: "刪除帳號失敗。"))
                        }
                    }
            } else {
                trySend(AuthResult.Error(task.exception?.localizedMessage ?: "重新驗證失敗。"))
            }
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
