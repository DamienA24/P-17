package com.openclassrooms.rebonnte

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.google.firebase.auth.FirebaseAuth
import com.openclassrooms.rebonnte.navigation.AuthenticatedShell
import com.openclassrooms.rebonnte.ui.auth.LoginScreen
import com.openclassrooms.rebonnte.ui.theme.RebonnteTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApp(isLoggedIn = auth.currentUser != null)
        }
    }
}

@Composable
fun MyApp(isLoggedIn: Boolean) {
    RebonnteTheme {
        var loggedIn by remember { mutableStateOf(isLoggedIn) }
        if (loggedIn) {
            AuthenticatedShell()
        } else {
            LoginScreen(onLoginSuccess = { loggedIn = true })
        }
    }
}
