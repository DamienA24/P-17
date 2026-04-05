package com.openclassrooms.rebonnte.ui.aisle.list

import com.openclassrooms.rebonnte.data.repository.AisleRepository
import com.openclassrooms.rebonnte.model.Aisle
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repo = mockk()
        every { repo.getAisles() } returns flowOf(emptyList())
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
        val viewModel = AisleViewModel(repo)
        advanceUntilIdle()
        assertEquals(aisles, viewModel.aisles.value)
    }

    @Test
    fun `addAisle calls repository with correct aisle`() = runTest {
        coEvery { repo.addAisle(any()) } returns Unit
        val viewModel = AisleViewModel(repo)
        viewModel.addAisle("Rayon Test")
        advanceUntilIdle()
        coVerify { repo.addAisle(Aisle(name = "Rayon Test")) }
    }

    @Test
    fun `isLoading is true initially then false after first emission`() = runTest {
        val aisles = listOf(Aisle(id = "1", name = "Rayon A"))
        every { repo.getAisles() } returns flowOf(aisles)
        val viewModel = AisleViewModel(repo)
        assertEquals(true, viewModel.isLoading.value)
        advanceUntilIdle()
        assertEquals(false, viewModel.isLoading.value)
    }

    @Test
    fun `errorMessage is set when flow throws`() = runTest {
        every { repo.getAisles() } returns flow { throw RuntimeException("Network error") }
        val viewModel = AisleViewModel(repo)
        advanceUntilIdle()
        assertEquals("Network error", viewModel.errorMessage.value)
        assertEquals(false, viewModel.isLoading.value)
    }
}
