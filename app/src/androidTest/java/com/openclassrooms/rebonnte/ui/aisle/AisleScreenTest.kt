package com.openclassrooms.rebonnte.ui.aisle

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.openclassrooms.rebonnte.data.repository.AisleRepository
import com.openclassrooms.rebonnte.model.Aisle
import com.openclassrooms.rebonnte.ui.theme.RebonnteTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test

class AisleScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private class FakeAisleRepository : AisleRepository {
        val addedAisles = mutableListOf<String>()
        override fun getAisles(): Flow<List<Aisle>> = flowOf(emptyList())
        override suspend fun addAisle(aisle: Aisle) { addedAisles.add(aisle.name) }
        override suspend fun deleteAisle(aisleId: String) {}
    }

    private fun launchScreen(repo: FakeAisleRepository = FakeAisleRepository()): FakeAisleRepository {
        val viewModel = AisleViewModel(repo)
        composeTestRule.setContent {
            RebonnteTheme {
                AisleScreen(onAisleClick = { _, _ -> }, onLogout = {}, viewModel = viewModel)
            }
        }
        return repo
    }

    @Test
    fun fabClickOpensAddDialog() {
        launchScreen()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithContentDescription("Ajouter un rayon").performClick()
        composeTestRule.onNodeWithText("Nouveau rayon").assertIsDisplayed()
    }

    @Test
    fun confirmWithBlankNameDoesNotDismissDialog() {
        launchScreen()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithContentDescription("Ajouter un rayon").performClick()
        composeTestRule.onNodeWithText("Sauvegarder").performClick()
        composeTestRule.onNodeWithText("Nouveau rayon").assertIsDisplayed()
    }

    @Test
    fun typeNameAndConfirmDismissesDialog() {
        launchScreen()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithContentDescription("Ajouter un rayon").performClick()
        composeTestRule.onNodeWithText("Nom du rayon").performTextInput("Rayon Test")
        composeTestRule.onNodeWithText("Sauvegarder").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Nouveau rayon").assertDoesNotExist()
    }

    @Test
    fun cancelDismissesDialogWithoutAdding() {
        val fakeRepo = launchScreen()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithContentDescription("Ajouter un rayon").performClick()
        composeTestRule.onNodeWithText("Annuler").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Nouveau rayon").assertDoesNotExist()
        assert(fakeRepo.addedAisles.isEmpty())
    }
}
