package com.threehibeybey.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.threehibeybey.repositories.HistoryRepository
import com.threehibeybey.repositories.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class PersonalViewModel(
    private val userRepository: UserRepository,
    private val historyRepository: HistoryRepository,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    private val _passwordChangeSuccess = MutableLiveData<Boolean>()
    val passwordChangeSuccess: LiveData<Boolean> = _passwordChangeSuccess

    private val _passwordChangeError = MutableLiveData<String?>()
    val passwordChangeError: LiveData<String?> = _passwordChangeError

    private val _emailChangeSuccess = MutableLiveData<Boolean>()
    val emailChangeSuccess: LiveData<Boolean> = _emailChangeSuccess

    private val _emailChangeError = MutableLiveData<String?>()
    val emailChangeError: LiveData<String?> = _emailChangeError

    private val _accountDeletionSuccess = MutableLiveData<Boolean>()
    val accountDeletionSuccess: LiveData<Boolean> = _accountDeletionSuccess

    private val _accountDeletionError = MutableLiveData<String?>()
    val accountDeletionError: LiveData<String?> = _accountDeletionError

    private val _historyAddSuccess = MutableLiveData<Boolean>()
    val historyAddSuccess: LiveData<Boolean> = _historyAddSuccess

    private val _historyAddError = MutableLiveData<String?>()
    val historyAddError: LiveData<String?> = _historyAddError

    fun changePassword(newPassword: String) {
        val user = firebaseAuth.currentUser
        if (user != null) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    user.updatePassword(newPassword).await()
                    _passwordChangeSuccess.postValue(true)
                    _passwordChangeError.postValue(null)
                } catch (e: Exception) {
                    _passwordChangeSuccess.postValue(false)
                    _passwordChangeError.postValue(e.message)
                }
            }
        } else {
            _passwordChangeError.postValue("User not logged in")
        }
    }

    fun changeEmail(newEmail: String) {
        val user = firebaseAuth.currentUser
        if (user != null) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    user.updateEmail(newEmail).await()
                    _emailChangeSuccess.postValue(true)
                    _emailChangeError.postValue(null)
                } catch (e: Exception) {
                    _emailChangeSuccess.postValue(false)
                    _emailChangeError.postValue(e.message)
                }
            }
        } else {
            _emailChangeError.postValue("User not logged in")
        }
    }

    fun deleteAccount() {
        val user = firebaseAuth.currentUser
        if (user != null) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    user.delete().await()
                    _accountDeletionSuccess.postValue(true)
                    _accountDeletionError.postValue(null)
                } catch (e: Exception) {
                    _accountDeletionSuccess.postValue(false)
                    _accountDeletionError.postValue(e.message)
                }
            }
        } else {
            _accountDeletionError.postValue("User not logged in")
        }
    }

    fun addHistoryRecord(record: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val success = historyRepository.addHistoryRecord(record)
            if (success) {
                _historyAddSuccess.postValue(true)
                _historyAddError.postValue(null)
            } else {
                _historyAddSuccess.postValue(false)
                _historyAddError.postValue("Failed to add history record")
            }
        }
    }
}
