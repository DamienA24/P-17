# Refacto Detail Screens Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Migrer AisleDetailActivity et MedicineDetailActivity vers des écrans Compose dans le NavHost, avec ViewModels Hilt dédiés, et supprimer le BroadcastReceiver inutile de MainActivity.

**Architecture:** Deux nouveaux @HiltViewModel (AisleDetailViewModel, MedicineDetailViewModel) injectés via SavedStateHandle. Deux nouveaux Composables remplacent les Activities. AuthenticatedShell gère toute la navigation via NavHost avec routes string.

**Tech Stack:** Kotlin, Jetpack Compose, Hilt, Compose Navigation, Firebase Firestore, MockK, kotlinx-coroutines-test

---

## File Map

| Action | Fichier |
|--------|---------|
| Modify | `app/src/main/java/com/openclassrooms/rebonnte/MainActivity.kt` |
| Modify | `app/src/main/java/com/openclassrooms/rebonnte/ui/aisle/AisleScreen.kt` |
| Modify | `app/src/main/java/com/openclassrooms/rebonnte/ui/medicine/MedicineScreen.kt` |
| Create | `app/src/main/java/com/openclassrooms/rebonnte/ui/aisle/AisleDetailScreen.kt` |
| Create | `app/src/main/java/com/openclassrooms/rebonnte/ui/medicine/MedicineDetailScreen.kt` |
| Create | `app/src/test/java/com/openclassrooms/rebonnte/ui/aisle/AisleDetailViewModelTest.kt` |
| Create | `app/src/test/java/com/openclassrooms/rebonnte/ui/medicine/MedicineDetailViewModelTest.kt` |
| Delete | `app/src/main/java/com/openclassrooms/rebonnte/ui/aisle/AisleDetailActivity.kt` |
| Delete | `app/src/main/java/com/openclassrooms/rebonnte/ui/medicine/MedicineDetailActivity.kt` |

---

## Task 1 — Supprimer startBroadcastReceiver de MainActivity

**Files:**
- Modify: `app/src/main/java/com/openclassrooms/rebonnte/MainActivity.kt`

- [ ] **Supprimer le champ `myBroadcastReceiver`, `onDestroy()`, `startBroadcastReceiver()` et la classe interne `MyBroadcastReceiver`**

Remplacer tout le contenu de `MainActivity` (lignes 51-89) par :

```kotlin
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
```

- [ ] **Supprimer les imports devenus inutiles** (lignes 3-11 dans le fichier) :

```kotlin
// Supprimer ces imports :
import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.widget.Toast
```

Garder : `android.content.Intent` (toujours utilisé), `android.os.Bundle`, et tous les imports Compose/Hilt.

- [ ] **Commit**

```bash
git add app/src/main/java/com/openclassrooms/rebonnte/MainActivity.kt
git commit -m "refactor: remove infinite BroadcastReceiver loop from MainActivity"
```

---

## Task 2 — Modifier AisleScreen et MedicineScreen (ajout lambdas de navigation)

**Files:**
- Modify: `app/src/main/java/com/openclassrooms/rebonnte/ui/aisle/AisleScreen.kt`
- Modify: `app/src/main/java/com/openclassrooms/rebonnte/ui/medicine/MedicineScreen.kt`

- [ ] **Remplacer entièrement `AisleScreen.kt`** :

```kotlin
package com.openclassrooms.rebonnte.ui.aisle

import com.openclassrooms.rebonnte.model.Aisle
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AisleScreen(
    viewModel: AisleViewModel,
    onAisleClick: (aisleId: String, aisleName: String) -> Unit
) {
    val aisles by viewModel.aisles.collectAsState(initial = emptyList())

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(aisles) { aisle ->
            AisleItem(aisle = aisle, onClick = { onAisleClick(aisle.id, aisle.name) })
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
        Icon(imageVector = Icons.Default.KeyboardArrowRight, contentDescription = "Arrow")
    }
}
```

- [ ] **Remplacer entièrement `MedicineScreen.kt`** :

```kotlin
package com.openclassrooms.rebonnte.ui.medicine

import com.openclassrooms.rebonnte.model.Medicine
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MedicineScreen(
    viewModel: MedicineViewModel,
    onMedicineClick: (medicineId: String, aisleId: String) -> Unit
) {
    val medicines by viewModel.medicines.collectAsState(initial = emptyList())

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(medicines) { medicine ->
            MedicineItem(medicine = medicine, onClick = { onMedicineClick(medicine.id, medicine.aisleId) })
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
        Icon(imageVector = Icons.Default.KeyboardArrowRight, contentDescription = "Arrow")
    }
}
```

- [ ] **Commit**

```bash
git add app/src/main/java/com/openclassrooms/rebonnte/ui/aisle/AisleScreen.kt \
        app/src/main/java/com/openclassrooms/rebonnte/ui/medicine/MedicineScreen.kt
git commit -m "refactor: replace Intent navigation with lambda callbacks in list screens"
```

---

## Task 3 — Tests AisleDetailViewModel (TDD — écrire avant l'implémentation)

**Files:**
- Create: `app/src/test/java/com/openclassrooms/rebonnte/ui/aisle/AisleDetailViewModelTest.kt`

- [ ] **Créer le fichier de test** :

```kotlin
package com.openclassrooms.rebonnte.ui.aisle

import androidx.lifecycle.SavedStateHandle
import com.openclassrooms.rebonnte.data.repository.MedicineRepository
import com.openclassrooms.rebonnte.model.Medicine
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AisleDetailViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() { Dispatchers.setMain(testDispatcher) }

    @After
    fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun `medicines emits list from repository for given aisleId`() = runTest {
        val expected = listOf(
            Medicine(id = "m1", name = "Aspirin", stock = 10, aisleId = "a1", aisleName = "Aisle 1")
        )
        val repo = mockk<MedicineRepository> {
            every { getMedicinesByAisle("a1") } returns flowOf(expected)
        }
        val vm = AisleDetailViewModel(SavedStateHandle(mapOf("aisleId" to "a1")), repo)

        assertEquals(expected, vm.medicines.value)
    }

    @Test
    fun `medicines starts empty before repository emits`() = runTest {
        val repo = mockk<MedicineRepository> {
            every { getMedicinesByAisle("a1") } returns flowOf(emptyList())
        }
        val vm = AisleDetailViewModel(SavedStateHandle(mapOf("aisleId" to "a1")), repo)

        assertEquals(emptyList<Medicine>(), vm.medicines.value)
    }
}
```

- [ ] **Vérifier que le test échoue (classe inexistante)**

```bash
./gradlew :app:testDebugUnitTest --tests "*.AisleDetailViewModelTest" 2>&1 | tail -20
```

Attendu : `error: unresolved reference: AisleDetailViewModel`

---

## Task 4 — Créer AisleDetailScreen.kt (ViewModel + Screen)

**Files:**
- Create: `app/src/main/java/com/openclassrooms/rebonnte/ui/aisle/AisleDetailScreen.kt`

- [ ] **Créer `AisleDetailScreen.kt`** :

```kotlin
package com.openclassrooms.rebonnte.ui.aisle

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openclassrooms.rebonnte.data.repository.MedicineRepository
import com.openclassrooms.rebonnte.model.Medicine
import com.openclassrooms.rebonnte.ui.medicine.MedicineItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AisleDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repo: MedicineRepository
) : ViewModel() {

    private val aisleId: String = checkNotNull(savedStateHandle["aisleId"])

    private val _medicines = MutableStateFlow<List<Medicine>>(emptyList())
    val medicines: StateFlow<List<Medicine>> = _medicines.asStateFlow()

    init {
        viewModelScope.launch {
            repo.getMedicinesByAisle(aisleId).collect { _medicines.value = it }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AisleDetailScreen(
    aisleName: String,
    onBack: () -> Unit,
    onMedicineClick: (medicineId: String, aisleId: String) -> Unit,
    viewModel: AisleDetailViewModel = hiltViewModel()
) {
    val medicines by viewModel.medicines.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(aisleName) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            contentPadding = padding,
            modifier = Modifier.fillMaxSize()
        ) {
            items(medicines) { medicine ->
                MedicineItem(
                    medicine = medicine,
                    onClick = { onMedicineClick(medicine.id, medicine.aisleId) }
                )
            }
        }
    }
}
```

- [ ] **Lancer les tests pour vérifier qu'ils passent**

```bash
./gradlew :app:testDebugUnitTest --tests "*.AisleDetailViewModelTest" 2>&1 | tail -10
```

Attendu : `2 tests completed`

- [ ] **Commit**

```bash
git add app/src/main/java/com/openclassrooms/rebonnte/ui/aisle/AisleDetailScreen.kt \
        app/src/test/java/com/openclassrooms/rebonnte/ui/aisle/AisleDetailViewModelTest.kt
git commit -m "feat: add AisleDetailViewModel and AisleDetailScreen with Hilt and Compose Navigation"
```

---

## Task 5 — Tests MedicineDetailViewModel (TDD — écrire avant l'implémentation)

**Files:**
- Create: `app/src/test/java/com/openclassrooms/rebonnte/ui/medicine/MedicineDetailViewModelTest.kt`

- [ ] **Créer le fichier de test** :

```kotlin
package com.openclassrooms.rebonnte.ui.medicine

import androidx.lifecycle.SavedStateHandle
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.openclassrooms.rebonnte.data.repository.MedicineRepository
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
            buildAuth(email = null)
        )

        vm.updateStock(-1)
        advanceUntilIdle()

        coVerify { repo.updateStock("m1", "a1", -1, "") }
    }
}
```

- [ ] **Vérifier que le test échoue (classe inexistante)**

```bash
./gradlew :app:testDebugUnitTest --tests "*.MedicineDetailViewModelTest" 2>&1 | tail -20
```

Attendu : `error: unresolved reference: MedicineDetailViewModel`

---

## Task 6 — Créer MedicineDetailScreen.kt (ViewModel + Screen)

**Files:**
- Create: `app/src/main/java/com/openclassrooms/rebonnte/ui/medicine/MedicineDetailScreen.kt`

- [ ] **Créer `MedicineDetailScreen.kt`** :

```kotlin
package com.openclassrooms.rebonnte.ui.medicine

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.openclassrooms.rebonnte.data.repository.MedicineRepository
import com.openclassrooms.rebonnte.model.History
import com.openclassrooms.rebonnte.model.Medicine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MedicineDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repo: MedicineRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val medicineId: String = checkNotNull(savedStateHandle["medicineId"])
    private val aisleId: String = checkNotNull(savedStateHandle["aisleId"])

    private val _medicine = MutableStateFlow<Medicine?>(null)
    val medicine: StateFlow<Medicine?> = _medicine.asStateFlow()

    private val _history = MutableStateFlow<List<History>>(emptyList())
    val history: StateFlow<List<History>> = _history.asStateFlow()

    init {
        viewModelScope.launch {
            repo.getMedicinesByAisle(aisleId).collect { medicines ->
                _medicine.value = medicines.find { it.id == medicineId }
            }
        }
        viewModelScope.launch {
            repo.getHistory(medicineId, aisleId).collect { _history.value = it }
        }
    }

    fun updateStock(delta: Int) {
        viewModelScope.launch {
            repo.updateStock(medicineId, aisleId, delta, auth.currentUser?.email ?: "")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicineDetailScreen(
    onBack: () -> Unit,
    viewModel: MedicineDetailViewModel = hiltViewModel()
) {
    val medicine by viewModel.medicine.collectAsState()
    val history by viewModel.history.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(medicine?.name ?: "") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        }
    ) { padding ->
        medicine?.let { med ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp)
            ) {
                TextField(
                    value = med.name,
                    onValueChange = {},
                    label = { Text("Name") },
                    enabled = false,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = med.aisleName,
                    onValueChange = {},
                    label = { Text("Aisle") },
                    enabled = false,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(onClick = { if (med.stock > 0) viewModel.updateStock(-1) }) {
                        Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "Minus One")
                    }
                    TextField(
                        value = med.stock.toString(),
                        onValueChange = {},
                        label = { Text("Stock") },
                        enabled = false,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { viewModel.updateStock(1) }) {
                        Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Plus One")
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("History", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(history) { h -> HistoryItem(history = h) }
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
            Text(text = "User: ${history.userEmail}")
            Text(text = "Date: ${history.date}")
            Text(text = "Details: ${history.details}")
        }
    }
}
```

- [ ] **Lancer les tests pour vérifier qu'ils passent**

```bash
./gradlew :app:testDebugUnitTest --tests "*.MedicineDetailViewModelTest" 2>&1 | tail -10
```

Attendu : `4 tests completed`

- [ ] **Commit**

```bash
git add app/src/main/java/com/openclassrooms/rebonnte/ui/medicine/MedicineDetailScreen.kt \
        app/src/test/java/com/openclassrooms/rebonnte/ui/medicine/MedicineDetailViewModelTest.kt
git commit -m "feat: add MedicineDetailViewModel and MedicineDetailScreen with Hilt and Compose Navigation"
```

---

## Task 7 — Mettre à jour AuthenticatedShell (routes + lambdas)

**Files:**
- Modify: `app/src/main/java/com/openclassrooms/rebonnte/MainActivity.kt`

- [ ] **Ajouter les imports manquants** en haut du fichier, après les imports existants :

```kotlin
import android.net.Uri
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.openclassrooms.rebonnte.ui.aisle.AisleDetailScreen
import com.openclassrooms.rebonnte.ui.medicine.MedicineDetailScreen
```

- [ ] **Remplacer le bloc `NavHost` dans `AuthenticatedShell`** (actuellement les 3 `composable(...)`) par :

```kotlin
NavHost(
    modifier = Modifier.padding(padding),
    navController = navController,
    startDestination = "aisle"
) {
    composable("aisle") {
        AisleScreen(
            viewModel = aisleViewModel,
            onAisleClick = { aisleId, aisleName ->
                navController.navigate("aisle_detail/$aisleId/${Uri.encode(aisleName)}")
            }
        )
    }
    composable("medicine") {
        MedicineScreen(
            viewModel = medicineViewModel,
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
```

- [ ] **Commit**

```bash
git add app/src/main/java/com/openclassrooms/rebonnte/MainActivity.kt
git commit -m "feat: wire aisle_detail and medicine_detail routes in AuthenticatedShell NavHost"
```

---

## Task 8 — Supprimer les anciennes Activities + vérifier la compilation

**Files:**
- Delete: `app/src/main/java/com/openclassrooms/rebonnte/ui/aisle/AisleDetailActivity.kt`
- Delete: `app/src/main/java/com/openclassrooms/rebonnte/ui/medicine/MedicineDetailActivity.kt`

- [ ] **Supprimer les deux fichiers**

```bash
rm app/src/main/java/com/openclassrooms/rebonnte/ui/aisle/AisleDetailActivity.kt
rm app/src/main/java/com/openclassrooms/rebonnte/ui/medicine/MedicineDetailActivity.kt
```

- [ ] **Vérifier qu'il n'y a plus de référence à ces classes**

```bash
grep -r "AisleDetailActivity\|MedicineDetailActivity" app/src/main/ --include="*.kt"
```

Attendu : aucun résultat.

- [ ] **Lancer la compilation complète**

```bash
./gradlew :app:assembleDebug 2>&1 | tail -20
```

Attendu : `BUILD SUCCESSFUL`

- [ ] **Lancer tous les tests unitaires**

```bash
./gradlew :app:testDebugUnitTest 2>&1 | tail -10
```

Attendu : tous les tests passent.

- [ ] **Commit final**

```bash
git add -A
git commit -m "refactor: delete AisleDetailActivity and MedicineDetailActivity, fully migrated to Compose Navigation"
```
