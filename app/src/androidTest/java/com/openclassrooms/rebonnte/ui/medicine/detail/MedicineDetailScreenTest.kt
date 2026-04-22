package com.openclassrooms.rebonnte.ui.medicine.detail

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.lifecycle.SavedStateHandle
import com.google.firebase.auth.FirebaseUser
import com.openclassrooms.rebonnte.data.repository.AisleRepository
import com.openclassrooms.rebonnte.data.repository.AuthRepository
import com.openclassrooms.rebonnte.data.repository.MedicineRepository
import com.openclassrooms.rebonnte.model.Aisle
import com.openclassrooms.rebonnte.model.History
import com.openclassrooms.rebonnte.model.Medicine
import com.openclassrooms.rebonnte.ui.theme.RebonnteTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test

class MedicineDetailScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private class FakeMedicineRepository(
        private val medicines: List<Medicine> = emptyList()
    ) : MedicineRepository {
        override fun getMedicines(): Flow<List<Medicine>> = flowOf(medicines)
        override fun getMedicinesByAisle(aisleId: String): Flow<List<Medicine>> = flowOf(medicines)
        override suspend fun addMedicine(medicine: Medicine) {}
        override suspend fun updateStock(medicineId: String, aisleId: String, delta: Int, userEmail: String) {}
        override suspend fun deleteMedicine(medicineId: String, aisleId: String) {}
        override fun getHistory(medicineId: String, aisleId: String): Flow<List<History>> = flowOf(emptyList())
    }

    private class FakeAisleRepository : AisleRepository {
        override fun getAisles(): Flow<List<Aisle>> = flowOf(emptyList())
        override suspend fun addAisle(aisle: Aisle) {}
        override suspend fun deleteAisle(aisleId: String) {}
    }

    private class FakeAuthRepository : AuthRepository {
        override fun currentUser(): FirebaseUser? = null
        override fun authStateFlow(): Flow<FirebaseUser?> = flowOf(null)
        override suspend fun signIn(email: String, password: String) = throw NotImplementedError()
        override suspend fun register(email: String, password: String) = throw NotImplementedError()
        override fun signOut() {}
    }

    private fun launchCreationMode() {
        val viewModel = MedicineDetailViewModel(
            savedStateHandle = SavedStateHandle(),
            repo = FakeMedicineRepository(),
            aisleRepo = FakeAisleRepository(),
            auth = FakeAuthRepository()
        )
        composeTestRule.setContent {
            RebonnteTheme {
                MedicineDetailScreen(onBack = {}, viewModel = viewModel)
            }
        }
    }

    private fun launchViewMode() {
        val medicine = Medicine(id = "med1", name = "Aspirine", stock = 10, aisleId = "a1", aisleName = "Rayon A")
        val viewModel = MedicineDetailViewModel(
            savedStateHandle = SavedStateHandle(mapOf("medicineId" to "med1", "aisleId" to "a1")),
            repo = FakeMedicineRepository(medicines = listOf(medicine)),
            aisleRepo = FakeAisleRepository(),
            auth = FakeAuthRepository()
        )
        composeTestRule.setContent {
            RebonnteTheme {
                MedicineDetailScreen(onBack = {}, viewModel = viewModel)
            }
        }
    }

    @Test
    fun creationModeShowsFormAndSaveButton() {
        launchCreationMode()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Nouveau médicament").assertIsDisplayed()
        composeTestRule.onNodeWithText("Sauvegarder").assertIsDisplayed()
    }

    @Test
    fun creationModeDoesNotShowDeleteButton() {
        launchCreationMode()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithContentDescription("Supprimer ce médicament").assertDoesNotExist()
    }

    @Test
    fun viewModeShowsDeleteButton() {
        launchViewMode()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithContentDescription("Supprimer ce médicament").assertIsDisplayed()
    }

    @Test
    fun viewModeDeleteClickShowsConfirmDialog() {
        launchViewMode()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithContentDescription("Supprimer ce médicament").performClick()
        composeTestRule.onNodeWithText("Supprimer ce médicament ?").assertIsDisplayed()
    }
}
