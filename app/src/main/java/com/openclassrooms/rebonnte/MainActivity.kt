package com.openclassrooms.rebonnte

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.firebase.auth.FirebaseAuth
import com.openclassrooms.rebonnte.ui.aisle.AisleDetailScreen
import com.openclassrooms.rebonnte.ui.aisle.AisleScreen
import com.openclassrooms.rebonnte.ui.auth.LoginScreen
import com.openclassrooms.rebonnte.ui.medicine.MedicineDetailScreen
import com.openclassrooms.rebonnte.ui.medicine.MedicineScreen
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

@Composable
fun AuthenticatedShell() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val route = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    label = { Text("Aisle") },
                    selected = route == "aisle",
                    onClick = { navController.navigate("aisle") }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.List, contentDescription = null) },
                    label = { Text("Medicine") },
                    selected = route == "medicine",
                    onClick = { navController.navigate("medicine") }
                )
            }
        },
    ) { padding ->
        NavHost(
            modifier = Modifier.padding(padding),
            navController = navController,
            startDestination = "aisle"
        ) {
            composable("aisle") {
                AisleScreen(
                    onAisleClick = { aisleId, aisleName ->
                        navController.navigate("aisle_detail/$aisleId/${Uri.encode(aisleName)}")
                    }
                )
            }
            composable("medicine") {
                MedicineScreen(
                    onMedicineClick = { medicineId, aisleId ->
                        navController.navigate("medicine_detail/$medicineId/$aisleId")
                    }
                )
            }
            composable(
                route = "aisle_detail/{aisleId}/{aisleName}",
                arguments = listOf(
                    navArgument("aisleId") { type = NavType.StringType },
                    navArgument("aisleName") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val aisleId = backStackEntry.arguments?.getString("aisleId") ?: ""
                val aisleName = backStackEntry.arguments?.getString("aisleName") ?: ""
                AisleDetailScreen(
                    aisleName = aisleName,
                    onBack = { navController.navigateUp() },
                    onMedicineClick = { medicineId, aisleId ->
                        navController.navigate("medicine_detail/$medicineId/$aisleId")
                    }
                )
            }
            composable(
                route = "medicine_detail/{medicineId}/{aisleId}",
                arguments = listOf(
                    navArgument("medicineId") { type = NavType.StringType },
                    navArgument("aisleId") { type = NavType.StringType }
                )
            ) {
                MedicineDetailScreen(onBack = { navController.navigateUp() })
            }
        }
    }
}

