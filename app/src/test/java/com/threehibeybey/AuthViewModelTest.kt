package com.threehibeybey

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.threehibeybey.repositories.AuthRepository
import com.threehibeybey.viewmodels.AuthState
import com.threehibeybey.viewmodels.AuthViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.*

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = UnconfinedTestDispatcher()

    @Test
    fun `login successful updates user and state to Success`() = runTest {
        val mockAuth = mock(FirebaseAuth::class.java)
        val mockUser = mock(FirebaseUser::class.java)
        `when`(mockAuth.currentUser).thenReturn(mockUser)

        val loginFlow = MutableSharedFlow<AuthRepository.AuthResult>()
        val mockRepo = mock(AuthRepository::class.java)
        `when`(mockRepo.login("test@example.com", "Password1")).thenReturn(loginFlow)

        val viewModel = AuthViewModel(mockRepo)

        val stateCollector = mutableListOf<AuthState>()
        val job = launch(testDispatcher) {
            viewModel.authState.collect {
                stateCollector.add(it)
            }
        }

        viewModel.login("test@example.com", "Password1")
        loginFlow.emit(AuthRepository.AuthResult.Success(mockUser))

        assertEquals(AuthState.Loading, stateCollector[0])
        assertEquals(AuthState.Success, stateCollector[1])

        job.cancel()
    }

    @Test
    fun `login failure updates state to Error`() = runTest {
        val mockAuth = mock(FirebaseAuth::class.java)

        val loginFlow = MutableSharedFlow<AuthRepository.AuthResult>()
        val mockRepo = mock(AuthRepository::class.java)
        `when`(mockRepo.login("test@example.com", "wrongpassword")).thenReturn(loginFlow)

        val viewModel = AuthViewModel(mockRepo)

        val stateCollector = mutableListOf<AuthState>()
        val job = launch(testDispatcher) {
            viewModel.authState.collect {
                stateCollector.add(it)
            }
        }

        viewModel.login("test@example.com", "wrongpassword")
        loginFlow.emit(AuthRepository.AuthResult.Error("Authentication failed."))

        assertEquals(AuthState.Loading, stateCollector[0])
        assertTrue(stateCollector[1] is AuthState.Error)
        assertEquals("Authentication failed.", (stateCollector[1] as AuthState.Error).message)

        job.cancel()
    }
}
