# Refacto Detail Screens + MainActivity Cleanup

**Date:** 2026-03-27
**Scope:** Migration AisleDetailActivity + MedicineDetailActivity → Compose Navigation, suppression du BroadcastReceiver

---

## Contexte

Les deux `DetailActivity` violent MVVM et SRP :
- `ViewModelProvider` sans Hilt (incohérent avec le reste du projet)
- Logique de filtrage dans les Composables (au lieu des ViewModels)
- `MedicineDetailActivity` ne persiste pas les modifications de stock (3 TODOs)
- `AisleDetailActivity` utilise `MedicineViewModel` sans ViewModel dédié
- `MedicineItem` défini en double avec signatures différentes
- `startBroadcastReceiver()` dans `MainActivity` crée une boucle infinie inutile

---

## Architecture cible

### Navigation

Deux routes ajoutées au `NavHost` de `AuthenticatedShell` :

```
"aisle_detail/{aisleId}/{aisleName}"    -- aisleId = @DocumentId Firestore
"medicine_detail/{medicineId}/{aisleId}"
```

- `AisleScreen` reçoit `onAisleClick: (aisleId: String, aisleName: String) -> Unit`
- `MedicineScreen` reçoit `onMedicineClick: (medicineId: String, aisleId: String) -> Unit`
- `AisleDetailScreen` reçoit `onMedicineClick: (medicineId: String, aisleId: String) -> Unit`
- Les lambdas sont fournies par `AuthenticatedShell` où vit le `navController`
- Navigation par **ID Firestore** (pas par nom) pour des requêtes Repository correctes

### Nouveaux ViewModels

**`AisleDetailViewModel`**
```
@HiltViewModel
constructor(savedStateHandle: SavedStateHandle, repo: MedicineRepository)
- aisleId depuis SavedStateHandle
- init {} → repo.getMedicinesByAisle(aisleId).collect { _medicines.value = it }
- val medicines: StateFlow<List<Medicine>>
```

**`MedicineDetailViewModel`**
```
@HiltViewModel
constructor(savedStateHandle: SavedStateHandle, repo: MedicineRepository, auth: FirebaseAuth)
- medicineId, aisleId depuis SavedStateHandle
- init {} → repo.getMedicinesByAisle(aisleId).collect { medicine = it.find { m -> m.id == medicineId } }
- init {} → repo.getHistory(medicineId, aisleId).collect { _history.value = it }
- val medicine: StateFlow<Medicine?>
- val history: StateFlow<List<History>>
- fun updateStock(delta: Int) → repo.updateStock(medicineId, aisleId, delta, auth.currentUser?.email ?: "")
```

> Aucune modification du `MedicineRepository` : `getMedicinesByAisle()`, `updateStock()` et `getHistory()` existent déjà.

### Nouveaux screens

**`AisleDetailScreen.kt`** (package `ui.aisle`)
- `hiltViewModel<AisleDetailViewModel>()`
- Params : `onBack: () -> Unit`, `onMedicineClick: (medicineId, aisleId) -> Unit`
- `Scaffold` avec `TopAppBar` (titre = `aisleName` passé en nav arg, bouton retour → `onBack()`)
- `LazyColumn` de `MedicineItem` → `onClick = { onMedicineClick(medicine.id, medicine.aisleId) }`
- Réutilise `MedicineItem` depuis `MedicineScreen.kt` (import cross-package)

**`MedicineDetailScreen.kt`** (package `ui.medicine`)
- `hiltViewModel<MedicineDetailViewModel>()`
- Param : `onBack: () -> Unit`
- `Scaffold` avec `TopAppBar` (titre = nom du médicament, bouton retour → `onBack()`)
- Champs read-only : nom, aisleName
- Ligne stock : `IconButton(-)` / `TextField(stock)` / `IconButton(+)` → `viewModel.updateStock(±1)`
- `LazyColumn` d'`HistoryItem` alimenté par `viewModel.history`
- `HistoryItem` reste dans ce fichier

---

## Fichiers supprimés

| Fichier | Raison |
|---|---|
| `ui/aisle/AisleDetailActivity.kt` | Remplacé par `AisleDetailScreen.kt` |
| `ui/medicine/MedicineDetailActivity.kt` | Remplacé par `MedicineDetailScreen.kt` |

---

## Fichiers modifiés

| Fichier | Changement |
|---|---|
| `MainActivity.kt` | Supprimer `startBroadcastReceiver()`, `MyBroadcastReceiver`, `onDestroy()` |
| `AuthenticatedShell` (dans `MainActivity.kt`) | Ajouter les 2 nouvelles routes au NavHost avec `onBack = { navController.navigateUp() }` ; passer lambdas de navigation à `AisleScreen` et `MedicineScreen` |
| `ui/aisle/AisleScreen.kt` | Ajouter param `onAisleClick: (aisleId, aisleName) -> Unit` ; supprimer `startDetailActivity()` |
| `ui/medicine/MedicineScreen.kt` | Ajouter param `onMedicineClick: (medicineId, aisleId) -> Unit` ; supprimer `startDetailActivity()` |

---

## Fichiers créés

| Fichier | Contenu |
|---|---|
| `ui/aisle/AisleDetailScreen.kt` | Composable + `AisleDetailViewModel` |
| `ui/medicine/MedicineDetailScreen.kt` | Composable + `MedicineDetailViewModel` |

---

## Nettoyage duplication

`MedicineItem` dans `AisleDetailActivity.kt` (signature `onClick: (String) -> Unit`) est supprimée avec le fichier.
`MedicineItem` dans `MedicineScreen.kt` (signature `onClick: () -> Unit`) est réutilisée dans `AisleDetailScreen` via import.

---

## Ordre d'implémentation

1. Supprimer `startBroadcastReceiver` + `onDestroy` de `MainActivity`
2. Modifier `AisleScreen` + `MedicineScreen` (ajout lambdas, suppression `startDetailActivity`)
3. Créer `AisleDetailScreen.kt` avec `AisleDetailViewModel`
4. Créer `MedicineDetailScreen.kt` avec `MedicineDetailViewModel`
5. Mettre à jour `AuthenticatedShell` : ajouter routes + lambdas
6. Supprimer `AisleDetailActivity.kt` et `MedicineDetailActivity.kt`
7. Vérifier la compilation

---

## Ce qui ne change pas

- `MedicineRepository` : aucune modification
- `AisleRepository` : aucune modification
- `MedicineViewModel` : aucune modification
- `AisleViewModel` : aucune modification
- `AuthViewModel` / `AuthRepository` : aucune modification
- Models, DI, Theme : aucune modification
