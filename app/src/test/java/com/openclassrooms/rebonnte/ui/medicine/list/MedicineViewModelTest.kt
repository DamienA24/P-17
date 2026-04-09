package com.openclassrooms.rebonnte.ui.medicine.list

import com.google.firebase.auth.FirebaseUser
import com.openclassrooms.rebonnte.data.repository.AuthRepository
import com.openclassrooms.rebonnte.data.repository.MedicineRepository
import com.openclassrooms.rebonnte.model.Medicine
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
class MedicineViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repo: MedicineRepository
    private lateinit var authRepo: AuthRepository
    private val loggedInUser: FirebaseUser = mockk()

    private val medicines = listOf(
        Medicine(id = "1", name = "Aspirine",   stock = 10, aisleId = "a1", aisleName = "Rayon A"),
        Medicine(id = "2", name = "Doliprane",  stock = 5,  aisleId = "a1", aisleName = "Rayon A"),
        Medicine(id = "3", name = "Ibuprofène", stock = 20, aisleId = "a2", aisleName = "Rayon B")
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repo = mockk()
        authRepo = mockk()
        every { repo.getMedicines() } returns flowOf(medicines)
        every { authRepo.authStateFlow() } returns flowOf(loggedInUser)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `filterByName does not lose source data`() = runTest {
        val viewModel = MedicineViewModel(repo, authRepo)
        advanceUntilIdle()
        viewModel.filterByName("asp")
        assertEquals(1, viewModel.medicines.value.size)
        assertEquals("Aspirine", viewModel.medicines.value[0].name)
        viewModel.filterByName("")
        assertEquals(3, viewModel.medicines.value.size)
    }

    @Test
    fun `sortByName sorts medicines alphabetically`() = runTest {
        val viewModel = MedicineViewModel(repo, authRepo)
        advanceUntilIdle()
        viewModel.sortByName()
        assertEquals("Aspirine",   viewModel.medicines.value[0].name)
        assertEquals("Doliprane",  viewModel.medicines.value[1].name)
        assertEquals("Ibuprofène", viewModel.medicines.value[2].name)
    }

    @Test
    fun `sortByStock sorts medicines by stock ascending`() = runTest {
        val viewModel = MedicineViewModel(repo, authRepo)
        advanceUntilIdle()
        viewModel.sortByStock()
        assertEquals(5,  viewModel.medicines.value[0].stock)
        assertEquals(10, viewModel.medicines.value[1].stock)
        assertEquals(20, viewModel.medicines.value[2].stock)
    }

    @Test
    fun `updateStock calls repository with correct parameters`() = runTest {
        coEvery { repo.updateStock(any(), any(), any(), any()) } returns Unit
        val viewModel = MedicineViewModel(repo, authRepo)
        viewModel.updateStock("1", "a1", 1, "user@test.com")
        advanceUntilIdle()
        coVerify { repo.updateStock("1", "a1", 1, "user@test.com") }
    }

    @Test
    fun `isLoading is true initially then false after first emission`() = runTest {
        val viewModel = MedicineViewModel(repo, authRepo)
        assertEquals(true, viewModel.isLoading.value)
        advanceUntilIdle()
        assertEquals(false, viewModel.isLoading.value)
    }

    @Test
    fun `errorMessage is set when flow throws`() = runTest {
        every { repo.getMedicines() } returns flow { throw RuntimeException("Network error") }
        val viewModel = MedicineViewModel(repo, authRepo)
        advanceUntilIdle()
        assertEquals("Network error", viewModel.errorMessage.value)
        assertEquals(false, viewModel.isLoading.value)
    }

    @Test
    fun `sortByName toggles to descending on second call`() = runTest {
        val viewModel = MedicineViewModel(repo, authRepo)
        advanceUntilIdle()
        viewModel.sortByName()
        viewModel.sortByName()
        assertEquals("Ibuprofène", viewModel.medicines.value[0].name)
        assertEquals("Doliprane",  viewModel.medicines.value[1].name)
        assertEquals("Aspirine",   viewModel.medicines.value[2].name)
    }

    @Test
    fun `sortByStock toggles to descending on second call`() = runTest {
        val viewModel = MedicineViewModel(repo, authRepo)
        advanceUntilIdle()
        viewModel.sortByStock()
        viewModel.sortByStock()
        assertEquals(20, viewModel.medicines.value[0].stock)
        assertEquals(10, viewModel.medicines.value[1].stock)
        assertEquals(5,  viewModel.medicines.value[2].stock)
    }

    @Test
    fun `sortByNone resets sort direction`() = runTest {
        val viewModel = MedicineViewModel(repo, authRepo)
        advanceUntilIdle()
        viewModel.sortByName()
        viewModel.sortByNone()
        viewModel.sortByName()
        assertEquals("Aspirine",   viewModel.medicines.value[0].name)
        assertEquals("Doliprane",  viewModel.medicines.value[1].name)
        assertEquals("Ibuprofène", viewModel.medicines.value[2].name)
    }

    @Test
    fun `medicines stop updating when auth state becomes null`() = runTest {
        val authStateFlow = MutableStateFlow<FirebaseUser?>(loggedInUser)
        every { authRepo.authStateFlow() } returns authStateFlow
        every { repo.getMedicines() } returns flowOf(medicines)

        val viewModel = MedicineViewModel(repo, authRepo)
        advanceUntilIdle()
        assertEquals(3, viewModel.medicines.value.size)

        authStateFlow.value = null
        advanceUntilIdle()

        coVerify(exactly = 1) { repo.getMedicines() }
    }
}
