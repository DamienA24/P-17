package com.openclassrooms.rebonnte.ui.auth

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.google.firebase.auth.FirebaseUser
import com.openclassrooms.rebonnte.data.repository.AuthRepository
import com.openclassrooms.rebonnte.ui.theme.RebonnteTheme
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import org.junit.Rule
import org.junit.Test

class LoginScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private fun launchScreen() {
        val viewModel = AuthViewModel(mockk<AuthRepository>(relaxed = true))
        composeTestRule.setContent {
            RebonnteTheme {
                LoginScreen(onLoginSuccess = {}, viewModel = viewModel)
            }
        }
    }

    @Test
    fun buttonsAreDisabledWhenFieldsAreEmpty() {
        launchScreen()
        composeTestRule.onNodeWithText("Se connecter").assertIsNotEnabled()
        composeTestRule.onNodeWithText("Créer un compte").assertIsNotEnabled()
    }

    @Test
    fun buttonsAreEnabledWhenBothFieldsHaveText() {
        launchScreen()
        composeTestRule.onNodeWithText("Email").performTextInput("test@test.com")
        composeTestRule.onNodeWithText("Mot de passe").performTextInput("password123")
        composeTestRule.onNodeWithText("Se connecter").assertIsEnabled()
        composeTestRule.onNodeWithText("Créer un compte").assertIsEnabled()
    }

    @Test
    fun errorMessageIsDisplayedWhenStateIsError() {
        val repo = mockk<AuthRepository>()
        coEvery { repo.signIn(any(), any()) } returns Result.failure(Exception("Email ou mot de passe incorrect"))
        val viewModel = AuthViewModel(repo)
        composeTestRule.setContent {
            RebonnteTheme {
                LoginScreen(onLoginSuccess = {}, viewModel = viewModel)
            }
        }
        composeTestRule.onNodeWithText("Email").performTextInput("test@test.com")
        composeTestRule.onNodeWithText("Mot de passe").performTextInput("wrongpassword")
        composeTestRule.onNodeWithText("Se connecter").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Email ou mot de passe incorrect").assertIsDisplayed()
    }

    @Test
    fun loadingIndicatorIsVisibleDuringSignIn() {
        val repo = mockk<AuthRepository>()
        val deferred = CompletableDeferred<Result<FirebaseUser>>()
        coEvery { repo.signIn(any(), any()) } coAnswers { deferred.await() }
        val viewModel = AuthViewModel(repo)
        composeTestRule.setContent {
            RebonnteTheme {
                LoginScreen(onLoginSuccess = {}, viewModel = viewModel)
            }
        }
        composeTestRule.onNodeWithText("Email").performTextInput("test@test.com")
        composeTestRule.onNodeWithText("Mot de passe").performTextInput("password123")
        composeTestRule.onNodeWithText("Se connecter").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithContentDescription("Chargement en cours").assertIsDisplayed()
        // Clean up
        deferred.cancel()
    }
}
