package com.openclassrooms.rebonnte.ui.auth

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.google.firebase.auth.FirebaseUser
import com.openclassrooms.rebonnte.data.repository.AuthRepository
import com.openclassrooms.rebonnte.ui.theme.RebonnteTheme
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test

class LoginScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private class FakeAuthRepository(
        private val signInAnswer: suspend () -> Result<FirebaseUser> = { throw RuntimeException("not configured") },
        private val registerAnswer: suspend () -> Result<FirebaseUser> = { throw RuntimeException("not configured") }
    ) : AuthRepository {
        override fun currentUser(): FirebaseUser? = null
        override fun authStateFlow(): Flow<FirebaseUser?> = flowOf(null)
        override suspend fun signIn(email: String, password: String) = signInAnswer()
        override suspend fun register(email: String, password: String) = registerAnswer()
        override fun signOut() {}
    }

    private fun launchScreen(repo: AuthRepository = FakeAuthRepository()) {
        val viewModel = AuthViewModel(repo)
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
        val repo = FakeAuthRepository(
            signInAnswer = { Result.failure(Exception("Email ou mot de passe incorrect")) }
        )
        launchScreen(repo)
        composeTestRule.onNodeWithText("Email").performTextInput("test@test.com")
        composeTestRule.onNodeWithText("Mot de passe").performTextInput("wrongpassword")
        composeTestRule.onNodeWithText("Se connecter").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Email ou mot de passe incorrect").assertIsDisplayed()
    }

    @Test
    fun loadingIndicatorIsVisibleDuringSignIn() {
        val neverCompletes = CompletableDeferred<Result<FirebaseUser>>()
        val repo = FakeAuthRepository(signInAnswer = { neverCompletes.await() })
        launchScreen(repo)
        composeTestRule.onNodeWithText("Email").performTextInput("test@test.com")
        composeTestRule.onNodeWithText("Mot de passe").performTextInput("password123")
        composeTestRule.onNodeWithText("Se connecter").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithContentDescription("Chargement en cours").assertIsDisplayed()
        neverCompletes.cancel()
    }
}
