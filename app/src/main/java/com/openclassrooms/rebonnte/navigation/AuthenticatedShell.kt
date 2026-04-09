package com.openclassrooms.rebonnte.navigation

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.hilt.navigation.compose.hiltViewModel
import com.openclassrooms.rebonnte.R
import com.openclassrooms.rebonnte.ui.aisle.detail.AisleDetailScreen
import com.openclassrooms.rebonnte.ui.aisle.list.AisleScreen
import com.openclassrooms.rebonnte.ui.auth.AuthViewModel
import com.openclassrooms.rebonnte.ui.medicine.detail.MedicineDetailScreen
import com.openclassrooms.rebonnte.ui.medicine.list.MedicineScreen

@Composable
fun AuthenticatedShell(onLogout: () -> Unit) {
    val authViewModel: AuthViewModel = hiltViewModel()
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val route = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    label = { Text(stringResource(R.string.nav_aisle)) },
                    selected = route == "aisle",
                    onClick = { navController.navigate("aisle") }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.List, contentDescription = null) },
                    label = { Text(stringResource(R.string.nav_medicine)) },
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
                    },
                    onLogout = {
                        onLogout()
                        authViewModel.signOut()
                    }
                )
            }
            composable("medicine") {
                MedicineScreen(
                    onMedicineClick = { medicineId, aisleId ->
                        navController.navigate("medicine_detail/$medicineId/$aisleId")
                    },
                    onAddMedicine = { navController.navigate("medicine_new") }
                )
            }
            composable(
                route = "aisle_detail/{aisleId}/{aisleName}",
                arguments = listOf(
                    navArgument("aisleId") { type = NavType.StringType },
                    navArgument("aisleName") { type = NavType.StringType }
                )
            ) { backStackEntry ->
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
            composable("medicine_new") {
                MedicineDetailScreen(onBack = { navController.navigateUp() })
            }
        }
    }
}
