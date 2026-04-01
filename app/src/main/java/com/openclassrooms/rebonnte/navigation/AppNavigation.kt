package com.openclassrooms.rebonnte.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.openclassrooms.rebonnte.ui.auth.LoginScreen
import com.openclassrooms.rebonnte.ui.theme.RebonnteTheme

@Composable
fun AppNavigation(isLoggedIn: Boolean) {
    RebonnteTheme {
        var loggedIn by remember { mutableStateOf(isLoggedIn) }
        if (loggedIn) {
            AuthenticatedShell(onLogout = { loggedIn = false })
        } else {
            LoginScreen(onLoginSuccess = { loggedIn = true })
        }
    }
}
