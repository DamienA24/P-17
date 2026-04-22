package com.openclassrooms.rebonnte.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.openclassrooms.rebonnte.ui.AppViewModel
import com.openclassrooms.rebonnte.ui.auth.LoginScreen
import com.openclassrooms.rebonnte.ui.theme.RebonnteTheme

@Composable
fun AppNavigation() {
    val appViewModel: AppViewModel = hiltViewModel()
    val isLoggedIn by appViewModel.isLoggedIn.collectAsState()
    RebonnteTheme {
        if (isLoggedIn) {
            MainScaffold(onLogout = {})
        } else {
            LoginScreen(onLoginSuccess = {})
        }
    }
}
