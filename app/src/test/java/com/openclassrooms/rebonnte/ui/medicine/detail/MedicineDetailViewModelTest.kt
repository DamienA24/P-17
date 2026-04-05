package com.openclassrooms.rebonnte.ui.medicine.detail

import androidx.lifecycle.SavedStateHandle
import com.google.firebase.auth.FirebaseUser
import com.openclassrooms.rebonnte.data.repository.AisleRepository
import com.openclassrooms.rebonnte.data.repository.AuthRepository
import com.openclassrooms.rebonnte.data.repository.MedicineRepository
import com.openclassrooms.rebonnte.model.Aisle
import com.openclassrooms.rebonnte.model.History
import com.openclassrooms.rebonnte.model.Medicine
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.Runs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MedicineDetailViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() { Dispatchers.setMain(testDispatcher) }

    @After
    fun tearDown() { Dispatchers.resetMain() }

    private fun buildRepo(
        medicines: List<Medicine> = emptyList(),
        history: List<History> = emptyList()
    ): MedicineRepository = mockk {
        every { getMedicinesByAisle("a1") } returns flowOf(medicines)
        every { getHistory("m1", "a1") } returns flowOf(history)
    }

    private fun buildAisleRepo(aisles: List<Aisle> = emptyList()): AisleRepository = mockk {
        every { getAisles() } returns flowOf(aisles)
    }

    private fun buildAuth(email: String? = null): AuthRepository = mockk {
        every { currentUser() } returns if (email != null) mockk<FirebaseUser> { every { this@mockk.email } returns email } else null
    }

    @Test
    fun `medicine emits the medicine matching medicineId`() = runTest {
        val medicines = listOf(
            Medicine(id = "m1", name = "Aspirin", stock = 10, aisleId = "a1", aisleName = "Aisle 1"),
            Medicine(id = "m2", name = "Ibuprofen", stock = 5, aisleId = "a1", aisleName = "Aisle 1")
        )
        val vm = MedicineDetailViewModel(
            SavedStateHandle(mapOf("medicineId" to "m1", "aisleId" to "a1")),
            buildRepo(medicines = medicines),
            buildAisleRepo(),
            buildAuth()
        )
        assertEquals(medicines[0], vm.medicine.value)
    }

    @Test
    fun `history emits list from repository`() = runTest {
        val historyList = listOf(
            History(id = "h1", medicineName = "Aspirin", details = "+1", stockBefore = 9, stockAfter = 10)
        )
        val vm = MedicineDetailViewModel(
            SavedStateHandle(mapOf("medicineId" to "m1", "aisleId" to "a1")),
            buildRepo(history = historyList),
            buildAisleRepo(),
            buildAuth()
        )
        assertEquals(historyList, vm.history.value)
    }

    @Test
    fun `updateStock calls repository with correct args including user email`() = runTest {
        val medicine = Medicine(id = "m1", name = "Aspirin", stock = 10, aisleId = "a1", aisleName = "Aisle 1")
        val repo = buildRepo(medicines = listOf(medicine)).also {
            coEvery { it.updateStock("m1", "a1", 1, "user@test.com") } just Runs
        }
        val vm = MedicineDetailViewModel(
            SavedStateHandle(mapOf("medicineId" to "m1", "aisleId" to "a1")),
            repo,
            buildAisleRepo(),
            buildAuth(email = "user@test.com")
        )
        vm.updateStock(1)
        advanceUntilIdle()
        coVerify { repo.updateStock("m1", "a1", 1, "user@test.com") }
    }

    @Test
    fun `updateStock uses empty string when user not logged in`() = runTest {
        val medicine = Medicine(id = "m1", name = "Aspirin", stock = 10, aisleId = "a1", aisleName = "Aisle 1")
        val repo = buildRepo(medicines = listOf(medicine)).also {
            coEvery { it.updateStock("m1", "a1", -1, "") } just Runs
        }
        val vm = MedicineDetailViewModel(
            SavedStateHandle(mapOf("medicineId" to "m1", "aisleId" to "a1")),
            repo,
            buildAisleRepo(),
            buildAuth(email = null)
        )
        vm.updateStock(-1)
        advanceUntilIdle()
        coVerify { repo.updateStock("m1", "a1", -1, "") }
    }

    @Test
    fun `saveMedicine in creation mode calls addMedicine and emits navigateBack`() = runTest {
        val aisleRepo = buildAisleRepo(listOf(Aisle(id = "a1", name = "Aisle 1")))
        val repo: MedicineRepository = mockk {
            coEvery { addMedicine(any()) } just Runs
        }
        val vm = MedicineDetailViewModel(
            SavedStateHandle(emptyMap()),
            repo,
            aisleRepo,
            buildAuth()
        )
        vm.updateFormName("Doliprane")
        vm.updateFormAisle("a1", "Aisle 1")
        vm.updateFormStock("10")

        val events = mutableListOf<Unit>()
        val job = launch { vm.navigateBack.collect { events.add(it) } }

        vm.saveMedicine()
        advanceUntilIdle()

        coVerify {
            repo.addMedicine(match {
                it.name == "Doliprane" && it.stock == 10 && it.aisleId == "a1" && it.aisleName == "Aisle 1"
            })
        }
        assertEquals(1, events.size)
        job.cancel()
    }

    @Test
    fun `deleteMedicine calls repository and emits navigateBack`() = runTest {
        val medicine = Medicine(id = "m1", name = "Aspirin", stock = 10, aisleId = "a1", aisleName = "Aisle 1")
        val repo = buildRepo(medicines = listOf(medicine)).also {
            coEvery { it.deleteMedicine("m1", "a1") } just Runs
        }
        val vm = MedicineDetailViewModel(
            SavedStateHandle(mapOf("medicineId" to "m1", "aisleId" to "a1")),
            repo,
            buildAisleRepo(),
            buildAuth()
        )

        val events = mutableListOf<Unit>()
        val job = launch { vm.navigateBack.collect { events.add(it) } }

        vm.deleteMedicine()
        advanceUntilIdle()

        coVerify { repo.deleteMedicine("m1", "a1") }
        assertEquals(1, events.size)
        job.cancel()
    }
}
