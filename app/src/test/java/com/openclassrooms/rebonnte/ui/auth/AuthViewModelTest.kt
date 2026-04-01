package com.openclassrooms.rebonnte.ui.auth

import com.google.firebase.auth.FirebaseUser
import com.openclassrooms.rebonnte.data.repository.AuthRepository
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repo: AuthRepository
    private lateinit var viewModel: AuthViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repo = mockk()
        viewModel = AuthViewModel(repo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `signIn success emits Success state`() = runTest {
        val fakeUser = mockk<FirebaseUser>()
        coEvery { repo.signIn(any(), any()) } returns Result.success(fakeUser)
        viewModel.signIn("test@test.com", "password123")
        advanceUntilIdle()
        assertEquals(AuthUiState.Success, viewModel.uiState.value)
    }

    @Test
    fun `signIn failure emits Error state`() = runTest {
        coEvery { repo.signIn(any(), any()) } returns Result.failure(Exception("Mot de passe incorrect"))
        viewModel.signIn("test@test.com", "wrong")
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is AuthUiState.Error)
    }

    @Test
    fun `register success emits Success state`() = runTest {
        val fakeUser = mockk<FirebaseUser>()
        coEvery { repo.register(any(), any()) } returns Result.success(fakeUser)
        viewModel.register("new@test.com", "password123")
        advanceUntilIdle()
        assertEquals(AuthUiState.Success, viewModel.uiState.value)
    }

    @Test
    fun `register duplicate email emits Error with message`() = runTest {
        val errorMsg = "L'adresse e-mail est déjà utilisée"
        coEvery { repo.register(any(), any()) } returns Result.failure(Exception(errorMsg))
        viewModel.register("existing@test.com", "password123")
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is AuthUiState.Error)
        assertEquals(errorMsg, (viewModel.uiState.value as AuthUiState.Error).message)
    }

    @Test
    fun `signOut calls repository signOut`() = runTest {
        every { repo.signOut() } just Runs
        viewModel.signOut()
        verify { repo.signOut() }
    }
}
