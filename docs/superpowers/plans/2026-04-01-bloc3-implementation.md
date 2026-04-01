# Bloc 3 + 4 (T09–T11, T13–T14) Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implémenter le formulaire de création de médicament, la suppression, la déconnexion, les indicateurs de chargement et corriger l'accessibilité.

**Architecture:** `MedicineDetailViewModel` gère deux modes (création/vue) via `medicineId: String?` nullable. Le logout est un callback passé de `AppNavigation` → `AuthenticatedShell` → `AisleScreen`. Les états loading/error sont ajoutés aux ViewModels existants.

**Tech Stack:** Kotlin, Jetpack Compose, Hilt, Firebase Auth/Firestore, MockK, kotlinx-coroutines-test

---

## File map

| Fichier | Action |
|---------|--------|
| `app/src/main/res/values/strings.xml` | Modify — ajouter 13 nouvelles strings |
| `ui/auth/AuthViewModel.kt` | Modify — ajouter `signOut()` |
| `ui/aisle/AisleViewModel.kt` | Modify — ajouter `isLoading`, `errorMessage` |
| `ui/aisle/AisleScreen.kt` | Modify — menu logout, loading state |
| `navigation/AuthenticatedShell.kt` | Modify — param `onLogout`, route `medicine_new`, passer `onAddMedicine` |
| `navigation/AppNavigation.kt` | Modify — passer `onLogout = { loggedIn = false }` |
| `ui/medicine/MedicineViewModel.kt` | Modify — ajouter `isLoading`, `errorMessage` |
| `ui/medicine/MedicineScreen.kt` | Modify — FAB, loading state, accessibilité |
| `ui/medicine/MedicineDetailViewModel.kt` | Modify — mode création/vue, `saveMedicine`, `deleteMedicine`, `navigateBack` |
| `ui/medicine/MedicineDetailScreen.kt` | Modify — UI création, bouton delete, dialog |
| `test/.../AuthViewModelTest.kt` | Modify — ajouter test `signOut` |
| `test/.../AisleViewModelTest.kt` | Modify — ajouter test `isLoading` |
| `test/.../MedicineViewModelTest.kt` | Modify — ajouter test `isLoading` |
| `test/.../MedicineDetailViewModelTest.kt` | Modify — ajouter `buildAisleRepo`, tests `saveMedicine`/`deleteMedicine` |

---

## Task 1 — T14 : Accessibilité + nouvelles strings

**Files:**
- Modify: `app/src/main/res/values/strings.xml`
- Modify: `app/src/main/java/com/openclassrooms/rebonnte/ui/medicine/MedicineScreen.kt`

- [ ] **Step 1 : Ajouter les nouvelles strings dans `strings.xml`**

Remplacer l'intégralité du fichier `app/src/main/res/values/strings.xml` :

```xml
<resources>
    <string name="app_name">rebonnte</string>

    <!-- Navigation -->
    <string name="nav_aisle">Aisle</string>
    <string name="nav_medicine">Medicine</string>

    <!-- Aisle screen -->
    <string name="aisle_screen_title">Aisle</string>
    <string name="add_aisle_cd">Add</string>
    <string name="new_aisle_name">Aisle %d</string>
    <string name="arrow_cd">Arrow</string>
    <string name="more_options_cd">Plus d\'options</string>
    <string name="logout_label">Se déconnecter</string>

    <!-- Aisle detail / back navigation -->
    <string name="back_cd">Retour</string>

    <!-- Medicine screen -->
    <string name="medicine_screen_title">Medicines</string>
    <string name="sort_by_none">Sort by None</string>
    <string name="sort_by_name">Sort by Name</string>
    <string name="sort_by_stock">Sort by Stock</string>
    <string name="search_placeholder">Search</string>
    <string name="search_cd">Rechercher</string>
    <string name="close_search_cd">Fermer la recherche</string>
    <string name="clear_search_cd">Effacer la recherche</string>
    <string name="add_medicine_cd">Ajouter un médicament</string>

    <!-- Medicine detail screen -->
    <string name="medicine_name_label">Name</string>
    <string name="medicine_aisle_label">Aisle</string>
    <string name="medicine_stock_label">Stock</string>
    <string name="decrement_stock_cd">Minus One</string>
    <string name="increment_stock_cd">Plus One</string>
    <string name="history_title">History</string>
    <string name="history_user">User: %s</string>
    <string name="history_date">Date: %s</string>
    <string name="history_details">Details: %s</string>
    <string name="new_medicine_title">Nouveau médicament</string>
    <string name="save_button">Sauvegarder</string>
    <string name="delete_medicine_cd">Supprimer ce médicament</string>
    <string name="delete_dialog_title">Supprimer ce médicament ?</string>
    <string name="delete_dialog_text">Cette action est irréversible.</string>
    <string name="delete_confirm_button">Supprimer</string>
    <string name="cancel_button">Annuler</string>

    <!-- Login screen -->
    <string name="app_title">Rebonnte</string>
    <string name="email_label">Email</string>
    <string name="password_label">Mot de passe</string>
    <string name="loading_cd">Chargement en cours</string>
    <string name="login_button">Se connecter</string>
    <string name="register_button">Créer un compte</string>
</resources>
```

- [ ] **Step 2 : Corriger les `contentDescription` null dans `EmbeddedSearchBar` et `MoreVert`**

Remplacer l'intégralité de `app/src/main/java/com/openclassrooms/rebonnte/ui/medicine/MedicineScreen.kt` :

```kotlin
package com.openclassrooms.rebonnte.ui.medicine

import com.openclassrooms.rebonnte.model.Medicine
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.openclassrooms.rebonnte.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicineScreen(
    onMedicineClick: (medicineId: String, aisleId: String) -> Unit,
    onAddMedicine: () -> Unit,
    viewModel: MedicineViewModel = hiltViewModel()
) {
    val medicines by viewModel.medicines.collectAsState()
    var isSearchActive by rememberSaveable { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            var expanded by remember { mutableStateOf(false) }
            Column(verticalArrangement = Arrangement.spacedBy((-1).dp)) {
                TopAppBar(
                    title = { Text(stringResource(R.string.medicine_screen_title)) },
                    actions = {
                        Box {
                            IconButton(onClick = { expanded = true }) {
                                Icon(Icons.Default.MoreVert, contentDescription = stringResource(R.string.more_options_cd))
                            }
                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false },
                                offset = DpOffset(x = 0.dp, y = 0.dp)
                            ) {
                                DropdownMenuItem(
                                    onClick = { viewModel.sortByNone(); expanded = false },
                                    text = { Text(stringResource(R.string.sort_by_none)) }
                                )
                                DropdownMenuItem(
                                    onClick = { viewModel.sortByName(); expanded = false },
                                    text = { Text(stringResource(R.string.sort_by_name)) }
                                )
                                DropdownMenuItem(
                                    onClick = { viewModel.sortByStock(); expanded = false },
                                    text = { Text(stringResource(R.string.sort_by_stock)) }
                                )
                            }
                        }
                    }
                )
                EmbeddedSearchBar(
                    query = searchQuery,
                    onQueryChange = {
                        viewModel.filterByName(it)
                        searchQuery = it
                    },
                    isSearchActive = isSearchActive,
                    onActiveChanged = { isSearchActive = it }
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddMedicine) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_medicine_cd))
            }
        }
    ) { padding ->
        LazyColumn(
            contentPadding = padding,
            modifier = Modifier.fillMaxSize()
        ) {
            items(medicines) { medicine ->
                MedicineItem(medicine = medicine, onClick = { onMedicineClick(medicine.id, medicine.aisleId) })
            }
        }
    }
}

@Composable
fun MedicineItem(medicine: Medicine, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(text = medicine.name, style = MaterialTheme.typography.bodyLarge)
            Text(text = "Stock: ${medicine.stock}", style = MaterialTheme.typography.bodyMedium)
        }
        Icon(imageVector = Icons.Default.KeyboardArrowRight, contentDescription = stringResource(R.string.arrow_cd))
    }
}

@Composable
fun EmbeddedSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    isSearchActive: Boolean,
    onActiveChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    var searchQuery by rememberSaveable { mutableStateOf(query) }
    val activeChanged: (Boolean) -> Unit = { active ->
        searchQuery = ""
        onQueryChange("")
        onActiveChanged(active)
    }
    val shape: Shape = RoundedCornerShape(16.dp)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .padding(horizontal = 16.dp)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isSearchActive) {
            IconButton(onClick = { activeChanged(false) }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = stringResource(R.string.close_search_cd),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        } else {
            Icon(
                imageVector = Icons.Rounded.Search,
                contentDescription = stringResource(R.string.search_cd),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        BasicTextField(
            value = searchQuery,
            onValueChange = { q ->
                searchQuery = q
                onQueryChange(q)
            },
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp),
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
            decorationBox = { innerTextField ->
                if (searchQuery.isEmpty()) {
                    Text(
                        text = stringResource(R.string.search_placeholder),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
                innerTextField()
            }
        )
        if (isSearchActive && searchQuery.isNotEmpty()) {
            IconButton(onClick = {
                searchQuery = ""
                onQueryChange("")
            }) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = stringResource(R.string.clear_search_cd),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
```

- [ ] **Step 3 : Compiler pour vérifier que les strings sont résolues**

```bash
./gradlew assembleDebug
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 4 : Commit**

```bash
git add app/src/main/res/values/strings.xml \
        app/src/main/java/com/openclassrooms/rebonnte/ui/medicine/MedicineScreen.kt
git commit -m "fix(a11y): add missing contentDescriptions on search and menu icons"
```

---

## Task 2 — T11 : Déconnexion

**Files:**
- Modify: `app/src/main/java/com/openclassrooms/rebonnte/ui/auth/AuthViewModel.kt`
- Modify: `app/src/test/java/com/openclassrooms/rebonnte/ui/auth/AuthViewModelTest.kt`
- Modify: `app/src/main/java/com/openclassrooms/rebonnte/ui/aisle/AisleScreen.kt`
- Modify: `app/src/main/java/com/openclassrooms/rebonnte/navigation/AuthenticatedShell.kt`
- Modify: `app/src/main/java/com/openclassrooms/rebonnte/navigation/AppNavigation.kt`

- [ ] **Step 1 : Écrire le test qui échoue pour `signOut`**

Ajouter dans `app/src/test/java/com/openclassrooms/rebonnte/ui/auth/AuthViewModelTest.kt`, dans la classe `AuthViewModelTest`, avant la dernière `}` :

```kotlin
    @Test
    fun `signOut calls repository signOut`() = runTest {
        every { repo.signOut() } just Runs
        viewModel.signOut()
        verify { repo.signOut() }
    }
```

Ajouter les imports manquants en haut du fichier (après les imports existants) :

```kotlin
import io.mockk.every
import io.mockk.just
import io.mockk.Runs
import io.mockk.verify
```

- [ ] **Step 2 : Vérifier que le test ne compile pas (méthode inexistante)**

```bash
./gradlew testDebugUnitTest --tests "com.openclassrooms.rebonnte.ui.auth.AuthViewModelTest.signOut calls repository signOut"
```

Expected: erreur de compilation `Unresolved reference: signOut`

- [ ] **Step 3 : Ajouter `signOut()` dans `AuthViewModel`**

Remplacer l'intégralité de `app/src/main/java/com/openclassrooms/rebonnte/ui/auth/AuthViewModel.kt` :

```kotlin
package com.openclassrooms.rebonnte.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openclassrooms.rebonnte.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AuthUiState {
    data object Idle : AuthUiState()
    data object Loading : AuthUiState()
    data object Success : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repo: AuthRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val result = repo.signIn(email, password)
            _uiState.value = if (result.isSuccess) {
                AuthUiState.Success
            } else {
                AuthUiState.Error(result.exceptionOrNull()?.message ?: "Erreur de connexion")
            }
        }
    }

    fun register(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val result = repo.register(email, password)
            _uiState.value = if (result.isSuccess) {
                AuthUiState.Success
            } else {
                AuthUiState.Error(result.exceptionOrNull()?.message ?: "Erreur d'inscription")
            }
        }
    }

    fun signOut() {
        repo.signOut()
    }
}
```

- [ ] **Step 4 : Vérifier que le test passe**

```bash
./gradlew testDebugUnitTest --tests "com.openclassrooms.rebonnte.ui.auth.AuthViewModelTest"
```

Expected: `BUILD SUCCESSFUL` — 5 tests passed

- [ ] **Step 5 : Mettre à jour `AisleScreen` avec le menu logout**

Remplacer l'intégralité de `app/src/main/java/com/openclassrooms/rebonnte/ui/aisle/AisleScreen.kt` :

```kotlin
package com.openclassrooms.rebonnte.ui.aisle

import com.openclassrooms.rebonnte.model.Aisle
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.openclassrooms.rebonnte.R
import com.openclassrooms.rebonnte.ui.auth.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AisleScreen(
    onAisleClick: (aisleId: String, aisleName: String) -> Unit,
    onLogout: () -> Unit,
    viewModel: AisleViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val aisles by viewModel.aisles.collectAsState()
    val newAisleName = stringResource(R.string.new_aisle_name, aisles.size + 1)
    var menuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.aisle_screen_title)) },
                actions = {
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = stringResource(R.string.more_options_cd))
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.logout_label)) },
                                onClick = {
                                    menuExpanded = false
                                    authViewModel.signOut()
                                    onLogout()
                                }
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.addAisle(newAisleName) }) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_aisle_cd))
            }
        }
    ) { padding ->
        LazyColumn(
            contentPadding = padding,
            modifier = Modifier.fillMaxSize()
        ) {
            items(aisles) { aisle ->
                AisleItem(aisle = aisle, onClick = { onAisleClick(aisle.id, aisle.name) })
            }
        }
    }
}

@Composable
fun AisleItem(aisle: Aisle, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = aisle.name, style = MaterialTheme.typography.bodyMedium)
        Icon(imageVector = Icons.Default.KeyboardArrowRight, contentDescription = stringResource(R.string.arrow_cd))
    }
}
```

- [ ] **Step 6 : Mettre à jour `AuthenticatedShell` pour accepter `onLogout` et passer le callback à `AisleScreen`**

Remplacer l'intégralité de `app/src/main/java/com/openclassrooms/rebonnte/navigation/AuthenticatedShell.kt` :

```kotlin
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
import com.openclassrooms.rebonnte.R
import com.openclassrooms.rebonnte.ui.aisle.AisleDetailScreen
import com.openclassrooms.rebonnte.ui.aisle.AisleScreen
import com.openclassrooms.rebonnte.ui.medicine.MedicineDetailScreen
import com.openclassrooms.rebonnte.ui.medicine.MedicineScreen

@Composable
fun AuthenticatedShell(onLogout: () -> Unit) {
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
                    onLogout = onLogout
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
            composable("medicine_new") {
                MedicineDetailScreen(onBack = { navController.navigateUp() })
            }
        }
    }
}
```

- [ ] **Step 7 : Mettre à jour `AppNavigation` pour passer `onLogout`**

Remplacer l'intégralité de `app/src/main/java/com/openclassrooms/rebonnte/navigation/AppNavigation.kt` :

```kotlin
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
```

- [ ] **Step 8 : Compiler**

```bash
./gradlew assembleDebug
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 9 : Commit**

```bash
git add app/src/main/java/com/openclassrooms/rebonnte/ui/auth/AuthViewModel.kt \
        app/src/test/java/com/openclassrooms/rebonnte/ui/auth/AuthViewModelTest.kt \
        app/src/main/java/com/openclassrooms/rebonnte/ui/aisle/AisleScreen.kt \
        app/src/main/java/com/openclassrooms/rebonnte/navigation/AuthenticatedShell.kt \
        app/src/main/java/com/openclassrooms/rebonnte/navigation/AppNavigation.kt
git commit -m "feat(auth): add logout button in aisle screen overflow menu"
```

---

## Task 3 — T09/T10 : MedicineDetailViewModel dual-mode

**Files:**
- Modify: `app/src/main/java/com/openclassrooms/rebonnte/ui/medicine/MedicineDetailViewModel.kt`
- Modify: `app/src/test/java/com/openclassrooms/rebonnte/ui/medicine/MedicineDetailViewModelTest.kt`

- [ ] **Step 1 : Écrire les tests qui échouent (saveMedicine + deleteMedicine)**

Remplacer l'intégralité de `app/src/test/java/com/openclassrooms/rebonnte/ui/medicine/MedicineDetailViewModelTest.kt` :

```kotlin
package com.openclassrooms.rebonnte.ui.medicine

import androidx.lifecycle.SavedStateHandle
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.openclassrooms.rebonnte.data.repository.AisleRepository
import com.openclassrooms.rebonnte.data.repository.MedicineRepository
import com.openclassrooms.rebonnte.model.Aisle
import com.openclassrooms.rebonnte.model.History
import com.openclassrooms.rebonnte.model.Medicine
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.Runs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MedicineDetailViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() { Dispatchers.setMain(testDispatcher) }

    @After
    fun tearDown() { Dispatchers.resetMain() }

    private fun buildRepo(
        medicines: List<Medicine> = emptyList(),
        history: List<History> = emptyList()
    ): MedicineRepository = mockk {
        every { getMedicinesByAisle("a1") } returns flowOf(medicines)
        every { getHistory("m1", "a1") } returns flowOf(history)
    }

    private fun buildAisleRepo(aisles: List<Aisle> = emptyList()): AisleRepository = mockk {
        every { getAisles() } returns flowOf(aisles)
    }

    private fun buildAuth(email: String? = null): FirebaseAuth = mockk {
        every { currentUser } returns if (email != null) mockk<FirebaseUser> { every { this@mockk.email } returns email } else null
    }

    @Test
    fun `medicine emits the medicine matching medicineId`() = runTest {
        val medicines = listOf(
            Medicine(id = "m1", name = "Aspirin", stock = 10, aisleId = "a1", aisleName = "Aisle 1"),
            Medicine(id = "m2", name = "Ibuprofen", stock = 5, aisleId = "a1", aisleName = "Aisle 1")
        )
        val vm = MedicineDetailViewModel(
            SavedStateHandle(mapOf("medicineId" to "m1", "aisleId" to "a1")),
            buildRepo(medicines = medicines),
            buildAisleRepo(),
            buildAuth()
        )
        assertEquals(medicines[0], vm.medicine.value)
    }

    @Test
    fun `history emits list from repository`() = runTest {
        val historyList = listOf(
            History(id = "h1", medicineName = "Aspirin", details = "+1", stockBefore = 9, stockAfter = 10)
        )
        val vm = MedicineDetailViewModel(
            SavedStateHandle(mapOf("medicineId" to "m1", "aisleId" to "a1")),
            buildRepo(history = historyList),
            buildAisleRepo(),
            buildAuth()
        )
        assertEquals(historyList, vm.history.value)
    }

    @Test
    fun `updateStock calls repository with correct args including user email`() = runTest {
        val medicine = Medicine(id = "m1", name = "Aspirin", stock = 10, aisleId = "a1", aisleName = "Aisle 1")
        val repo = buildRepo(medicines = listOf(medicine)).also {
            coEvery { it.updateStock("m1", "a1", 1, "user@test.com") } just Runs
        }
        val vm = MedicineDetailViewModel(
            SavedStateHandle(mapOf("medicineId" to "m1", "aisleId" to "a1")),
            repo,
            buildAisleRepo(),
            buildAuth(email = "user@test.com")
        )
        vm.updateStock(1)
        advanceUntilIdle()
        coVerify { repo.updateStock("m1", "a1", 1, "user@test.com") }
    }

    @Test
    fun `updateStock uses empty string when user not logged in`() = runTest {
        val medicine = Medicine(id = "m1", name = "Aspirin", stock = 10, aisleId = "a1", aisleName = "Aisle 1")
        val repo = buildRepo(medicines = listOf(medicine)).also {
            coEvery { it.updateStock("m1", "a1", -1, "") } just Runs
        }
        val vm = MedicineDetailViewModel(
            SavedStateHandle(mapOf("medicineId" to "m1", "aisleId" to "a1")),
            repo,
            buildAisleRepo(),
            buildAuth(email = null)
        )
        vm.updateStock(-1)
        advanceUntilIdle()
        coVerify { repo.updateStock("m1", "a1", -1, "") }
    }

    @Test
    fun `saveMedicine in creation mode calls addMedicine and emits navigateBack`() = runTest {
        val aisleRepo = buildAisleRepo(listOf(Aisle(id = "a1", name = "Aisle 1")))
        val repo: MedicineRepository = mockk {
            coEvery { addMedicine(any()) } just Runs
        }
        val vm = MedicineDetailViewModel(
            SavedStateHandle(emptyMap()),
            repo,
            aisleRepo,
            buildAuth()
        )
        vm.updateFormName("Doliprane")
        vm.updateFormAisle("a1", "Aisle 1")
        vm.updateFormStock("10")

        val events = mutableListOf<Unit>()
        val job = launch { vm.navigateBack.collect { events.add(it) } }

        vm.saveMedicine()
        advanceUntilIdle()

        coVerify {
            repo.addMedicine(match {
                it.name == "Doliprane" && it.stock == 10 && it.aisleId == "a1" && it.aisleName == "Aisle 1"
            })
        }
        assertEquals(1, events.size)
        job.cancel()
    }

    @Test
    fun `deleteMedicine calls repository and emits navigateBack`() = runTest {
        val medicine = Medicine(id = "m1", name = "Aspirin", stock = 10, aisleId = "a1", aisleName = "Aisle 1")
        val repo = buildRepo(medicines = listOf(medicine)).also {
            coEvery { it.deleteMedicine("m1", "a1") } just Runs
        }
        val vm = MedicineDetailViewModel(
            SavedStateHandle(mapOf("medicineId" to "m1", "aisleId" to "a1")),
            repo,
            buildAisleRepo(),
            buildAuth()
        )

        val events = mutableListOf<Unit>()
        val job = launch { vm.navigateBack.collect { events.add(it) } }

        vm.deleteMedicine()
        advanceUntilIdle()

        coVerify { repo.deleteMedicine("m1", "a1") }
        assertEquals(1, events.size)
        job.cancel()
    }
}
```

- [ ] **Step 2 : Vérifier que les tests échouent (compilation KO)**

```bash
./gradlew testDebugUnitTest --tests "com.openclassrooms.rebonnte.ui.medicine.MedicineDetailViewModelTest"
```

Expected: erreur de compilation — constructor arg mismatch, `saveMedicine`/`deleteMedicine`/`navigateBack` non définis

- [ ] **Step 3 : Implémenter le nouveau `MedicineDetailViewModel`**

Remplacer l'intégralité de `app/src/main/java/com/openclassrooms/rebonnte/ui/medicine/MedicineDetailViewModel.kt` :

```kotlin
package com.openclassrooms.rebonnte.ui.medicine

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.openclassrooms.rebonnte.data.repository.AisleRepository
import com.openclassrooms.rebonnte.data.repository.MedicineRepository
import com.openclassrooms.rebonnte.model.Aisle
import com.openclassrooms.rebonnte.model.History
import com.openclassrooms.rebonnte.model.Medicine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MedicineDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repo: MedicineRepository,
    private val aisleRepo: AisleRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val medicineId: String? = savedStateHandle["medicineId"]
    private val aisleId: String? = savedStateHandle["aisleId"]

    val isCreationMode: Boolean = medicineId == null

    private val _medicine = MutableStateFlow<Medicine?>(null)
    val medicine: StateFlow<Medicine?> = _medicine.asStateFlow()

    private val _history = MutableStateFlow<List<History>>(emptyList())
    val history: StateFlow<List<History>> = _history.asStateFlow()

    private val _aisles = MutableStateFlow<List<Aisle>>(emptyList())
    val aisles: StateFlow<List<Aisle>> = _aisles.asStateFlow()

    private val _formName = MutableStateFlow("")
    val formName: StateFlow<String> = _formName.asStateFlow()

    private val _formStock = MutableStateFlow(0)
    val formStock: StateFlow<Int> = _formStock.asStateFlow()

    private val _formAisleId = MutableStateFlow("")
    val formAisleId: StateFlow<String> = _formAisleId.asStateFlow()

    private val _formAisleName = MutableStateFlow("")

    private val _navigateBack = MutableSharedFlow<Unit>()
    val navigateBack: SharedFlow<Unit> = _navigateBack.asSharedFlow()

    init {
        if (isCreationMode) {
            viewModelScope.launch {
                aisleRepo.getAisles().collect { _aisles.value = it }
            }
        } else {
            viewModelScope.launch {
                repo.getMedicinesByAisle(aisleId!!).collect { medicines ->
                    _medicine.value = medicines.find { it.id == medicineId }
                }
            }
            viewModelScope.launch {
                repo.getHistory(medicineId!!, aisleId!!).collect { _history.value = it }
            }
        }
    }

    fun updateStock(delta: Int) {
        viewModelScope.launch {
            repo.updateStock(medicineId!!, aisleId!!, delta, auth.currentUser?.email ?: "")
        }
    }

    fun updateFormName(name: String) { _formName.value = name }

    fun updateFormStock(stock: String) { _formStock.value = stock.toIntOrNull() ?: 0 }

    fun updateFormAisle(aisleId: String, aisleName: String) {
        _formAisleId.value = aisleId
        _formAisleName.value = aisleName
    }

    fun saveMedicine() {
        viewModelScope.launch {
            repo.addMedicine(
                Medicine(
                    name = _formName.value,
                    stock = _formStock.value,
                    aisleId = _formAisleId.value,
                    aisleName = _formAisleName.value
                )
            )
            _navigateBack.emit(Unit)
        }
    }

    fun deleteMedicine() {
        viewModelScope.launch {
            repo.deleteMedicine(medicineId!!, aisleId!!)
            _navigateBack.emit(Unit)
        }
    }
}
```

- [ ] **Step 4 : Vérifier que tous les tests passent**

```bash
./gradlew testDebugUnitTest --tests "com.openclassrooms.rebonnte.ui.medicine.MedicineDetailViewModelTest"
```

Expected: `BUILD SUCCESSFUL` — 6 tests passed

- [ ] **Step 5 : Commit**

```bash
git add app/src/main/java/com/openclassrooms/rebonnte/ui/medicine/MedicineDetailViewModel.kt \
        app/src/test/java/com/openclassrooms/rebonnte/ui/medicine/MedicineDetailViewModelTest.kt
git commit -m "feat(medicine): add creation mode, delete, navigateBack to MedicineDetailViewModel"
```

---

## Task 4 — T09/T10 : MedicineDetailScreen (création + suppression)

**Files:**
- Modify: `app/src/main/java/com/openclassrooms/rebonnte/ui/medicine/MedicineDetailScreen.kt`

- [ ] **Step 1 : Remplacer `MedicineDetailScreen` par la version dual-mode**

Remplacer l'intégralité de `app/src/main/java/com/openclassrooms/rebonnte/ui/medicine/MedicineDetailScreen.kt` :

```kotlin
package com.openclassrooms.rebonnte.ui.medicine

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.openclassrooms.rebonnte.R
import com.openclassrooms.rebonnte.model.History

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicineDetailScreen(
    onBack: () -> Unit,
    viewModel: MedicineDetailViewModel = hiltViewModel()
) {
    val medicine by viewModel.medicine.collectAsState()
    val history by viewModel.history.collectAsState()
    val aisles by viewModel.aisles.collectAsState()
    val formName by viewModel.formName.collectAsState()
    val formStock by viewModel.formStock.collectAsState()
    val formAisleId by viewModel.formAisleId.collectAsState()

    var showDeleteDialog by remember { mutableStateOf(false) }
    var aisleMenuExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.navigateBack.collect { onBack() }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.delete_dialog_title)) },
            text = { Text(stringResource(R.string.delete_dialog_text)) },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    viewModel.deleteMedicine()
                }) { Text(stringResource(R.string.delete_confirm_button)) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.cancel_button))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (viewModel.isCreationMode) stringResource(R.string.new_medicine_title)
                        else medicine?.name ?: ""
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back_cd))
                    }
                },
                actions = {
                    if (!viewModel.isCreationMode) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete_medicine_cd))
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (viewModel.isCreationMode) {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp)
                    .fillMaxSize()
            ) {
                TextField(
                    value = formName,
                    onValueChange = { viewModel.updateFormName(it) },
                    label = { Text(stringResource(R.string.medicine_name_label)) },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                ExposedDropdownMenuBox(
                    expanded = aisleMenuExpanded,
                    onExpandedChange = { aisleMenuExpanded = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextField(
                        value = aisles.find { it.id == formAisleId }?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.medicine_aisle_label)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = aisleMenuExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = aisleMenuExpanded,
                        onDismissRequest = { aisleMenuExpanded = false }
                    ) {
                        aisles.forEach { aisle ->
                            DropdownMenuItem(
                                text = { Text(aisle.name) },
                                onClick = {
                                    viewModel.updateFormAisle(aisle.id, aisle.name)
                                    aisleMenuExpanded = false
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = if (formStock == 0) "" else formStock.toString(),
                    onValueChange = { viewModel.updateFormStock(it) },
                    label = { Text(stringResource(R.string.medicine_stock_label)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.weight(1f))
                Button(
                    onClick = { viewModel.saveMedicine() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.save_button))
                }
            }
        } else {
            medicine?.let { med ->
                Column(
                    modifier = Modifier
                        .padding(padding)
                        .padding(16.dp)
                ) {
                    TextField(
                        value = med.name,
                        onValueChange = {},
                        label = { Text(stringResource(R.string.medicine_name_label)) },
                        enabled = false,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TextField(
                        value = med.aisleName,
                        onValueChange = {},
                        label = { Text(stringResource(R.string.medicine_aisle_label)) },
                        enabled = false,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        IconButton(onClick = { if (med.stock > 0) viewModel.updateStock(-1) }) {
                            Icon(Icons.Filled.KeyboardArrowDown, contentDescription = stringResource(R.string.decrement_stock_cd))
                        }
                        TextField(
                            value = med.stock.toString(),
                            onValueChange = {},
                            label = { Text(stringResource(R.string.medicine_stock_label)) },
                            enabled = false,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { viewModel.updateStock(1) }) {
                            Icon(Icons.Default.KeyboardArrowUp, contentDescription = stringResource(R.string.increment_stock_cd))
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(stringResource(R.string.history_title), style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(history) { h -> HistoryItem(history = h) }
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryItem(history: History) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = history.medicineName, fontWeight = FontWeight.Bold)
            Text(text = stringResource(R.string.history_user, history.userEmail))
            Text(text = stringResource(R.string.history_date, history.date))
            Text(text = stringResource(R.string.history_details, history.details))
        }
    }
}
```

- [ ] **Step 2 : Compiler**

```bash
./gradlew assembleDebug
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 3 : Commit**

```bash
git add app/src/main/java/com/openclassrooms/rebonnte/ui/medicine/MedicineDetailScreen.kt
git commit -m "feat(medicine): add creation form and delete confirmation dialog in MedicineDetailScreen"
```

---

## Task 5 — T13 : Loading states dans MedicineViewModel et AisleViewModel

**Files:**
- Modify: `app/src/main/java/com/openclassrooms/rebonnte/ui/medicine/MedicineViewModel.kt`
- Modify: `app/src/main/java/com/openclassrooms/rebonnte/ui/aisle/AisleViewModel.kt`
- Modify: `app/src/main/java/com/openclassrooms/rebonnte/ui/medicine/MedicineScreen.kt`
- Modify: `app/src/main/java/com/openclassrooms/rebonnte/ui/aisle/AisleScreen.kt`
- Modify: `app/src/test/java/com/openclassrooms/rebonnte/ui/medicine/MedicineViewModelTest.kt`
- Modify: `app/src/test/java/com/openclassrooms/rebonnte/ui/aisle/AisleViewModelTest.kt`

- [ ] **Step 1 : Écrire les tests qui échouent dans `MedicineViewModelTest`**

Ajouter dans `MedicineViewModelTest.kt` :
- l'import `import kotlinx.coroutines.flow.flow` (avec les autres imports flow)
- les deux tests avant la dernière `}` de la classe :

```kotlin
    @Test
    fun `isLoading is true initially then false after first emission`() = runTest {
        val viewModel = MedicineViewModel(repo)
        assertEquals(true, viewModel.isLoading.value)
        advanceUntilIdle()
        assertEquals(false, viewModel.isLoading.value)
    }

    @Test
    fun `errorMessage is set when flow throws`() = runTest {
        every { repo.getMedicines() } returns flow { throw RuntimeException("Network error") }
        val viewModel = MedicineViewModel(repo)
        advanceUntilIdle()
        assertEquals("Network error", viewModel.errorMessage.value)
        assertEquals(false, viewModel.isLoading.value)
    }
```

- [ ] **Step 2 : Écrire les tests qui échouent dans `AisleViewModelTest`**

Ajouter dans `AisleViewModelTest.kt` :
- l'import `import kotlinx.coroutines.flow.flow` (avec les autres imports flow)
- les deux tests avant la dernière `}` de la classe :

```kotlin
    @Test
    fun `isLoading is true initially then false after first emission`() = runTest {
        val aisles = listOf(Aisle(id = "1", name = "Rayon A"))
        every { repo.getAisles() } returns flowOf(aisles)
        val viewModel = AisleViewModel(repo)
        assertEquals(true, viewModel.isLoading.value)
        advanceUntilIdle()
        assertEquals(false, viewModel.isLoading.value)
    }

    @Test
    fun `errorMessage is set when flow throws`() = runTest {
        every { repo.getAisles() } returns flow { throw RuntimeException("Network error") }
        val viewModel = AisleViewModel(repo)
        advanceUntilIdle()
        assertEquals("Network error", viewModel.errorMessage.value)
        assertEquals(false, viewModel.isLoading.value)
    }
```

- [ ] **Step 3 : Vérifier que les tests échouent (propriétés inexistantes)**

```bash
./gradlew testDebugUnitTest --tests "com.openclassrooms.rebonnte.ui.medicine.MedicineViewModelTest"
./gradlew testDebugUnitTest --tests "com.openclassrooms.rebonnte.ui.aisle.AisleViewModelTest"
```

Expected: erreur de compilation — `isLoading` et `errorMessage` non définis

- [ ] **Step 4 : Mettre à jour `MedicineViewModel`**

Remplacer l'intégralité de `app/src/main/java/com/openclassrooms/rebonnte/ui/medicine/MedicineViewModel.kt` :

```kotlin
package com.openclassrooms.rebonnte.ui.medicine

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openclassrooms.rebonnte.data.repository.MedicineRepository
import com.openclassrooms.rebonnte.model.Medicine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class MedicineViewModel @Inject constructor(
    private val repo: MedicineRepository
) : ViewModel() {
    private val _allMedicines = MutableStateFlow<List<Medicine>>(emptyList())
    private val _medicines = MutableStateFlow<List<Medicine>>(emptyList())
    val medicines: StateFlow<List<Medicine>> = _medicines.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        viewModelScope.launch {
            repo.getMedicines()
                .catch { e ->
                    _isLoading.value = false
                    _errorMessage.value = e.message ?: "Erreur inconnue"
                }
                .collect {
                    _isLoading.value = false
                    _allMedicines.value = it
                    _medicines.value = it
                }
        }
    }

    fun filterByName(name: String) {
        _medicines.value = if (name.isBlank()) {
            _allMedicines.value
        } else {
            _allMedicines.value.filter {
                it.name.lowercase(Locale.getDefault())
                    .contains(name.lowercase(Locale.getDefault()))
            }
        }
    }

    fun sortByNone() { _medicines.value = _allMedicines.value }

    fun sortByName() { _medicines.value = _allMedicines.value.sortedBy { it.name } }

    fun sortByStock() { _medicines.value = _allMedicines.value.sortedBy { it.stock } }

    fun updateStock(medicineId: String, aisleId: String, delta: Int, userEmail: String) {
        viewModelScope.launch {
            repo.updateStock(medicineId, aisleId, delta, userEmail)
        }
    }
}
```

- [ ] **Step 5 : Mettre à jour `AisleViewModel`**

Remplacer l'intégralité de `app/src/main/java/com/openclassrooms/rebonnte/ui/aisle/AisleViewModel.kt` :

```kotlin
package com.openclassrooms.rebonnte.ui.aisle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openclassrooms.rebonnte.data.repository.AisleRepository
import com.openclassrooms.rebonnte.model.Aisle
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AisleViewModel @Inject constructor(
    private val repo: AisleRepository
) : ViewModel() {
    private val _aisles = MutableStateFlow<List<Aisle>>(emptyList())
    val aisles: StateFlow<List<Aisle>> = _aisles.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        viewModelScope.launch {
            repo.getAisles()
                .catch { e ->
                    _isLoading.value = false
                    _errorMessage.value = e.message ?: "Erreur inconnue"
                }
                .collect {
                    _isLoading.value = false
                    _aisles.value = it
                }
        }
    }

    fun addAisle(name: String) {
        viewModelScope.launch {
            repo.addAisle(Aisle(name = name))
        }
    }
}
```

- [ ] **Step 6 : Vérifier que tous les tests passent**

```bash
./gradlew testDebugUnitTest --tests "com.openclassrooms.rebonnte.ui.medicine.MedicineViewModelTest"
./gradlew testDebugUnitTest --tests "com.openclassrooms.rebonnte.ui.aisle.AisleViewModelTest"
```

Expected: `BUILD SUCCESSFUL` — 6 tests passed pour MedicineViewModel, 4 tests passed pour AisleViewModel

- [ ] **Step 7 : Afficher le loading dans `MedicineScreen` — remplacer le fichier entier**

Remplacer l'intégralité de `app/src/main/java/com/openclassrooms/rebonnte/ui/medicine/MedicineScreen.kt` (version finale avec accessibilité + FAB + loading) :

```kotlin
package com.openclassrooms.rebonnte.ui.medicine

import com.openclassrooms.rebonnte.model.Medicine
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.openclassrooms.rebonnte.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicineScreen(
    onMedicineClick: (medicineId: String, aisleId: String) -> Unit,
    onAddMedicine: () -> Unit,
    viewModel: MedicineViewModel = hiltViewModel()
) {
    val medicines by viewModel.medicines.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    var isSearchActive by rememberSaveable { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            var expanded by remember { mutableStateOf(false) }
            Column(verticalArrangement = Arrangement.spacedBy((-1).dp)) {
                TopAppBar(
                    title = { Text(stringResource(R.string.medicine_screen_title)) },
                    actions = {
                        Box {
                            IconButton(onClick = { expanded = true }) {
                                Icon(Icons.Default.MoreVert, contentDescription = stringResource(R.string.more_options_cd))
                            }
                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false },
                                offset = DpOffset(x = 0.dp, y = 0.dp)
                            ) {
                                DropdownMenuItem(
                                    onClick = { viewModel.sortByNone(); expanded = false },
                                    text = { Text(stringResource(R.string.sort_by_none)) }
                                )
                                DropdownMenuItem(
                                    onClick = { viewModel.sortByName(); expanded = false },
                                    text = { Text(stringResource(R.string.sort_by_name)) }
                                )
                                DropdownMenuItem(
                                    onClick = { viewModel.sortByStock(); expanded = false },
                                    text = { Text(stringResource(R.string.sort_by_stock)) }
                                )
                            }
                        }
                    }
                )
                EmbeddedSearchBar(
                    query = searchQuery,
                    onQueryChange = {
                        viewModel.filterByName(it)
                        searchQuery = it
                    },
                    isSearchActive = isSearchActive,
                    onActiveChanged = { isSearchActive = it }
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddMedicine) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_medicine_cd))
            }
        }
    ) { padding ->
        when {
            isLoading -> Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }
            errorMessage != null -> Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) { Text(errorMessage!!, color = MaterialTheme.colorScheme.error) }
            else -> LazyColumn(
                contentPadding = padding,
                modifier = Modifier.fillMaxSize()
            ) {
                items(medicines) { medicine ->
                    MedicineItem(medicine = medicine, onClick = { onMedicineClick(medicine.id, medicine.aisleId) })
                }
            }
        }
    }
}

@Composable
fun MedicineItem(medicine: Medicine, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(text = medicine.name, style = MaterialTheme.typography.bodyLarge)
            Text(text = "Stock: ${medicine.stock}", style = MaterialTheme.typography.bodyMedium)
        }
        Icon(imageVector = Icons.Default.KeyboardArrowRight, contentDescription = stringResource(R.string.arrow_cd))
    }
}

@Composable
fun EmbeddedSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    isSearchActive: Boolean,
    onActiveChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    var searchQuery by rememberSaveable { mutableStateOf(query) }
    val activeChanged: (Boolean) -> Unit = { active ->
        searchQuery = ""
        onQueryChange("")
        onActiveChanged(active)
    }
    val shape: Shape = RoundedCornerShape(16.dp)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .padding(horizontal = 16.dp)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isSearchActive) {
            IconButton(onClick = { activeChanged(false) }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = stringResource(R.string.close_search_cd),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        } else {
            Icon(
                imageVector = Icons.Rounded.Search,
                contentDescription = stringResource(R.string.search_cd),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        BasicTextField(
            value = searchQuery,
            onValueChange = { q ->
                searchQuery = q
                onQueryChange(q)
            },
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp),
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
            decorationBox = { innerTextField ->
                if (searchQuery.isEmpty()) {
                    Text(
                        text = stringResource(R.string.search_placeholder),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
                innerTextField()
            }
        )
        if (isSearchActive && searchQuery.isNotEmpty()) {
            IconButton(onClick = {
                searchQuery = ""
                onQueryChange("")
            }) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = stringResource(R.string.clear_search_cd),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
```

- [ ] **Step 8 : Afficher le loading dans `AisleScreen` — remplacer le fichier entier**

Remplacer l'intégralité de `app/src/main/java/com/openclassrooms/rebonnte/ui/aisle/AisleScreen.kt` (version finale avec logout + loading) :

```kotlin
package com.openclassrooms.rebonnte.ui.aisle

import com.openclassrooms.rebonnte.model.Aisle
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.openclassrooms.rebonnte.R
import com.openclassrooms.rebonnte.ui.auth.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AisleScreen(
    onAisleClick: (aisleId: String, aisleName: String) -> Unit,
    onLogout: () -> Unit,
    viewModel: AisleViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val aisles by viewModel.aisles.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val newAisleName = stringResource(R.string.new_aisle_name, aisles.size + 1)
    var menuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.aisle_screen_title)) },
                actions = {
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = stringResource(R.string.more_options_cd))
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.logout_label)) },
                                onClick = {
                                    menuExpanded = false
                                    authViewModel.signOut()
                                    onLogout()
                                }
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.addAisle(newAisleName) }) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_aisle_cd))
            }
        }
    ) { padding ->
        when {
            isLoading -> Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }
            errorMessage != null -> Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) { Text(errorMessage!!, color = MaterialTheme.colorScheme.error) }
            else -> LazyColumn(
                contentPadding = padding,
                modifier = Modifier.fillMaxSize()
            ) {
                items(aisles) { aisle ->
                    AisleItem(aisle = aisle, onClick = { onAisleClick(aisle.id, aisle.name) })
                }
            }
        }
    }
}

@Composable
fun AisleItem(aisle: Aisle, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = aisle.name, style = MaterialTheme.typography.bodyMedium)
        Icon(imageVector = Icons.Default.KeyboardArrowRight, contentDescription = stringResource(R.string.arrow_cd))
    }
}
```

- [ ] **Step 9 : Compiler et lancer tous les tests**

```bash
./gradlew assembleDebug && ./gradlew testDebugUnitTest
```

Expected: `BUILD SUCCESSFUL` — tous les tests passent

- [ ] **Step 10 : Commit**

```bash
git add app/src/main/java/com/openclassrooms/rebonnte/ui/medicine/MedicineViewModel.kt \
        app/src/main/java/com/openclassrooms/rebonnte/ui/aisle/AisleViewModel.kt \
        app/src/main/java/com/openclassrooms/rebonnte/ui/medicine/MedicineScreen.kt \
        app/src/main/java/com/openclassrooms/rebonnte/ui/aisle/AisleScreen.kt \
        app/src/test/java/com/openclassrooms/rebonnte/ui/medicine/MedicineViewModelTest.kt \
        app/src/test/java/com/openclassrooms/rebonnte/ui/aisle/AisleViewModelTest.kt
git commit -m "feat(ux): add loading indicator and error state to medicine and aisle screens"
```

---

## Vérification finale

- [ ] **Lancer tous les tests unitaires**

```bash
./gradlew testDebugUnitTest
```

Expected: `BUILD SUCCESSFUL` — tous les tests passent (AuthViewModelTest: 5, AisleViewModelTest: 4, MedicineViewModelTest: 6, MedicineDetailViewModelTest: 6)

- [ ] **Mettre à jour `docs/tasks.md`**

Mettre les statuts suivants dans `docs/tasks.md` :
- T09 : `[x]`
- T10 : `[x]`
- T11 : `[x]`
- T13 : `[x]`
- T14 : `[x]`
- Mettre à jour le tableau résumé : Bloc 3 → 3/4, Bloc 4 → 1/2
