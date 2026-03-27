package com.openclassrooms.rebonnte.ui.aisle

import androidx.lifecycle.SavedStateHandle
import com.openclassrooms.rebonnte.data.repository.MedicineRepository
import com.openclassrooms.rebonnte.model.Medicine
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AisleDetailViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() { Dispatchers.setMain(testDispatcher) }

    @After
    fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun `medicines emits list from repository for given aisleId`() = runTest {
        val expected = listOf(
            Medicine(id = "m1", name = "Aspirin", stock = 10, aisleId = "a1", aisleName = "Aisle 1")
        )
        val repo = mockk<MedicineRepository> {
            every { getMedicinesByAisle("a1") } returns flowOf(expected)
        }
        val vm = AisleDetailViewModel(SavedStateHandle(mapOf("aisleId" to "a1")), repo)

        assertEquals(expected, vm.medicines.value)
    }

    @Test
    fun `medicines is empty when repository emits empty list`() = runTest {
        val repo = mockk<MedicineRepository> {
            every { getMedicinesByAisle("a1") } returns flowOf(emptyList())
        }
        val vm = AisleDetailViewModel(SavedStateHandle(mapOf("aisleId" to "a1")), repo)

        assertEquals(emptyList<Medicine>(), vm.medicines.value)
    }
}
