package com.openclassrooms.rebonnte.ui.auth

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.openclassrooms.rebonnte.data.repository.AuthRepository
import com.openclassrooms.rebonnte.ui.theme.RebonnteTheme
import io.mockk.mockk
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
}
