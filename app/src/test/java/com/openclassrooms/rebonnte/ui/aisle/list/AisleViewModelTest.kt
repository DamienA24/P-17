package com.openclassrooms.rebonnte.ui.aisle.list

import com.google.firebase.auth.FirebaseUser
import com.openclassrooms.rebonnte.data.repository.AisleRepository
import com.openclassrooms.rebonnte.data.repository.AuthRepository
import com.openclassrooms.rebonnte.model.Aisle
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AisleViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repo: AisleRepository
    private lateinit var authRepo: AuthRepository
    private val loggedInUser: FirebaseUser = mockk()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repo = mockk()
        authRepo = mockk()
        every { repo.getAisles() } returns flowOf(emptyList())
        every { authRepo.authStateFlow() } returns flowOf(loggedInUser)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `aisles flow is collected and exposed`() = runTest {
        val aisles = listOf(
            Aisle(id = "1", name = "Rayon A"),
            Aisle(id = "2", name = "Rayon B")
        )
        every { repo.getAisles() } returns flowOf(aisles)
        val viewModel = AisleViewModel(repo, authRepo)
        advanceUntilIdle()
        assertEquals(aisles, viewModel.aisles.value)
    }

    @Test
    fun `addAisle calls repository with correct aisle`() = runTest {
        coEvery { repo.addAisle(any()) } returns Unit
        val viewModel = AisleViewModel(repo, authRepo)
        viewModel.addAisle("Rayon Test")
        advanceUntilIdle()
        coVerify { repo.addAisle(Aisle(name = "Rayon Test")) }
    }

    @Test
    fun `isLoading is true initially then false after first emission`() = runTest {
        val aisles = listOf(Aisle(id = "1", name = "Rayon A"))
        every { repo.getAisles() } returns flowOf(aisles)
        val viewModel = AisleViewModel(repo, authRepo)
        assertEquals(true, viewModel.isLoading.value)
        advanceUntilIdle()
        assertEquals(false, viewModel.isLoading.value)
    }

    @Test
    fun `errorMessage is set when flow throws`() = runTest {
        every { repo.getAisles() } returns flow { throw RuntimeException("Network error") }
        val viewModel = AisleViewModel(repo, authRepo)
        advanceUntilIdle()
        assertEquals("Network error", viewModel.errorMessage.value)
        assertEquals(false, viewModel.isLoading.value)
    }

    @Test
    fun `aisles stop updating when auth state becomes null`() = runTest {
        val aisles = listOf(Aisle(id = "1", name = "Rayon A"))
        val authStateFlow = MutableStateFlow<FirebaseUser?>(loggedInUser)
        every { authRepo.authStateFlow() } returns authStateFlow
        every { repo.getAisles() } returns flowOf(aisles)

        val viewModel = AisleViewModel(repo, authRepo)
        advanceUntilIdle()
        assertEquals(aisles, viewModel.aisles.value)

        authStateFlow.value = null
        advanceUntilIdle()

        // getAisles() was only called once (before logout) — not re-subscribed after null
        coVerify(exactly = 1) { repo.getAisles() }
    }
}
