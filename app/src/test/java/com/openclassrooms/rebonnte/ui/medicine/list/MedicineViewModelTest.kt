package com.openclassrooms.rebonnte.ui.medicine.list

import com.openclassrooms.rebonnte.data.repository.MedicineRepository
import com.openclassrooms.rebonnte.model.Medicine
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
class MedicineViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repo: MedicineRepository

    private val medicines = listOf(
        Medicine(id = "1", name = "Aspirine",   stock = 10, aisleId = "a1", aisleName = "Rayon A"),
        Medicine(id = "2", name = "Doliprane",  stock = 5,  aisleId = "a1", aisleName = "Rayon A"),
        Medicine(id = "3", name = "Ibuprofène", stock = 20, aisleId = "a2", aisleName = "Rayon B")
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repo = mockk()
        every { repo.getMedicines() } returns flowOf(medicines)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `filterByName does not lose source data`() = runTest {
        val viewModel = MedicineViewModel(repo)
        advanceUntilIdle()
        viewModel.filterByName("asp")
        assertEquals(1, viewModel.medicines.value.size)
        assertEquals("Aspirine", viewModel.medicines.value[0].name)
        // Vider le filtre — les 3 médicaments doivent réapparaître
        viewModel.filterByName("")
        assertEquals(3, viewModel.medicines.value.size)
    }

    @Test
    fun `sortByName sorts medicines alphabetically`() = runTest {
        val viewModel = MedicineViewModel(repo)
        advanceUntilIdle()
        viewModel.sortByName()
        assertEquals("Aspirine",   viewModel.medicines.value[0].name)
        assertEquals("Doliprane",  viewModel.medicines.value[1].name)
        assertEquals("Ibuprofène", viewModel.medicines.value[2].name)
    }

    @Test
    fun `sortByStock sorts medicines by stock ascending`() = runTest {
        val viewModel = MedicineViewModel(repo)
        advanceUntilIdle()
        viewModel.sortByStock()
        assertEquals(5,  viewModel.medicines.value[0].stock)
        assertEquals(10, viewModel.medicines.value[1].stock)
        assertEquals(20, viewModel.medicines.value[2].stock)
    }

    @Test
    fun `updateStock calls repository with correct parameters`() = runTest {
        coEvery { repo.updateStock(any(), any(), any(), any()) } returns Unit
        val viewModel = MedicineViewModel(repo)
        viewModel.updateStock("1", "a1", 1, "user@test.com")
        advanceUntilIdle()
        coVerify { repo.updateStock("1", "a1", 1, "user@test.com") }
    }

    @Test
    fun `isLoading is true initially then false after first emission`() = runTest {
        val viewModel = MedicineViewModel(repo)
        assertEquals(true, viewModel.isLoading.value)
        advanceUntilIdle()
        assertEquals(false, viewModel.isLoading.value)
    }

    @Test
    fun `errorMessage is set when flow throws`() = runTest {
        every { repo.getMedicines() } returns flow { throw RuntimeException("Network error") }
        val viewModel = MedicineViewModel(repo)
        advanceUntilIdle()
        assertEquals("Network error", viewModel.errorMessage.value)
        assertEquals(false, viewModel.isLoading.value)
    }

    @Test
    fun `sortByName toggles to descending on second call`() = runTest {
        val viewModel = MedicineViewModel(repo)
        advanceUntilIdle()
        viewModel.sortByName() // ascending
        viewModel.sortByName() // descending
        assertEquals("Ibuprofène", viewModel.medicines.value[0].name)
        assertEquals("Doliprane",  viewModel.medicines.value[1].name)
        assertEquals("Aspirine",   viewModel.medicines.value[2].name)
    }

    @Test
    fun `sortByStock toggles to descending on second call`() = runTest {
        val viewModel = MedicineViewModel(repo)
        advanceUntilIdle()
        viewModel.sortByStock() // ascending
        viewModel.sortByStock() // descending
        assertEquals(20, viewModel.medicines.value[0].stock)
        assertEquals(10, viewModel.medicines.value[1].stock)
        assertEquals(5,  viewModel.medicines.value[2].stock)
    }

    @Test
    fun `sortByNone resets sort direction`() = runTest {
        val viewModel = MedicineViewModel(repo)
        advanceUntilIdle()
        viewModel.sortByName() // ascending → flag flipped to false
        viewModel.sortByNone() // resets flag back to true
        viewModel.sortByName() // should be ascending again
        assertEquals("Aspirine",   viewModel.medicines.value[0].name)
        assertEquals("Doliprane",  viewModel.medicines.value[1].name)
        assertEquals("Ibuprofène", viewModel.medicines.value[2].name)
    }
}
