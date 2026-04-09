package com.openclassrooms.rebonnte.ui.aisle.detail

import androidx.lifecycle.SavedStateHandle
import com.google.firebase.auth.FirebaseUser
import com.openclassrooms.rebonnte.data.repository.AuthRepository
import com.openclassrooms.rebonnte.data.repository.MedicineRepository
import com.openclassrooms.rebonnte.model.Medicine
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
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
    private val loggedInUser: FirebaseUser = mockk()
    private lateinit var authRepo: AuthRepository

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        authRepo = mockk()
        every { authRepo.authStateFlow() } returns flowOf(loggedInUser)
    }

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
        val vm = AisleDetailViewModel(SavedStateHandle(mapOf("aisleId" to "a1")), repo, authRepo)

        assertEquals(expected, vm.medicines.value)
    }

    @Test
    fun `medicines is empty when repository emits empty list`() = runTest {
        val repo = mockk<MedicineRepository> {
            every { getMedicinesByAisle("a1") } returns flowOf(emptyList())
        }
        val vm = AisleDetailViewModel(SavedStateHandle(mapOf("aisleId" to "a1")), repo, authRepo)

        assertEquals(emptyList<Medicine>(), vm.medicines.value)
    }

    @Test
    fun `medicines stop updating when auth state becomes null`() = runTest {
        val medicines = listOf(
            Medicine(id = "m1", name = "Aspirin", stock = 10, aisleId = "a1", aisleName = "Aisle 1")
        )
        val authStateFlow = MutableStateFlow<FirebaseUser?>(loggedInUser)
        every { authRepo.authStateFlow() } returns authStateFlow
        val repo = mockk<MedicineRepository> {
            every { getMedicinesByAisle("a1") } returns flowOf(medicines)
        }

        val vm = AisleDetailViewModel(SavedStateHandle(mapOf("aisleId" to "a1")), repo, authRepo)
        assertEquals(medicines, vm.medicines.value)

        authStateFlow.value = null

        verify(exactly = 1) { repo.getMedicinesByAisle("a1") }
    }
}
