# Spec — Bloc 3 + Bloc 4 partiel (T09–T11, T13–T14)

**Date :** 2026-04-01  
**Projet :** Rebonnte — Gestion de stock médicaments  
**Tâches couvertes :** T09, T10, T11, T13, T14

---

## Contexte

Les Blocs 1 et 2 sont complets (auth Firebase, repositories Firestore, MVVM, corrections de bugs). Ce spec couvre les fonctionnalités manquantes prioritaires et les améliorations qualité restantes.

---

## T11 — Bouton de déconnexion

### Objectif
Permettre à l'utilisateur de se déconnecter depuis l'écran principal.

### Design

**`AuthViewModel`**
- Nouvelle méthode `signOut()` qui appelle `repo.signOut()`
- Pas de nouveau state — la déconnexion est fire-and-forget

**`AuthenticatedShell`**
- Nouveau paramètre `onLogout: () -> Unit`
- Passe ce callback à `AisleScreen`

**`AisleScreen`**
- Nouveau paramètre `onLogout: () -> Unit`
- `TopAppBar` gagne un `DropdownMenu` avec une entrée "Se déconnecter"
- Au clic : `authViewModel.signOut()` puis `onLogout()`
- `AuthViewModel` est injecté via `hiltViewModel()` dans `AisleScreen`

**`AppNavigation`**
- Passe `onLogout = { loggedIn = false }` à `AuthenticatedShell`
- Le passage à `loggedIn = false` déclenche l'affichage de `LoginScreen`

---

## T09 — Formulaire de création de médicament

### Objectif
Remplacer la création aléatoire par un formulaire utilisateur : nom, rayon, stock initial.

### Design

**Nouvelle route**
- Route `medicine_new` ajoutée dans `AuthenticatedShell`
- Navigue vers `MedicineDetailScreen` sans passer de `medicineId` ni `aisleId`

**`MedicineScreen`**
- FAB ajouté avec icône `Add` et `contentDescription = "Ajouter un médicament"`
- Au clic : `navController.navigate("medicine_new")`

**`MedicineDetailViewModel`**
- `medicineId` et `aisleId` deviennent `String?` (nullable via `SavedStateHandle.get<String>()`)
- `val isCreationMode: Boolean = medicineId == null`
- En mode création :
  - Injecte `AisleRepository` pour charger `aisles: StateFlow<List<Aisle>>`
  - Expose les champs de formulaire : `formName`, `formStock`, `formAisleId`, `formAisleName` comme `MutableStateFlow`
  - Méthode `updateFormName(name: String)`, `updateFormStock(stock: Int)`, `updateFormAisle(aisleId: String, aisleName: String)`
  - Méthode `saveMedicine()` : crée un `Medicine` et appelle `repo.addMedicine()`, puis émet sur `navigateBack`
- En mode vue : comportement existant inchangé
- `navigateBack: SharedFlow<Unit>` partagé entre création (après save) et suppression (après delete)

**`MedicineDetailScreen`**
- Collecte `viewModel.navigateBack` via `LaunchedEffect` → appelle `onBack()`
- Rendu conditionnel sur `viewModel.isCreationMode` :
  - **Mode création** :
    - `TextField` éditable pour le nom (`formName`)
    - `ExposedDropdownMenuBox` pour le rayon (liste `aisles` du VM)
    - `TextField` éditable pour le stock initial (`formStock`, clavier numérique)
    - Bouton `Button` pleine largeur en bas → `viewModel.saveMedicine()`
    - Pas d'historique, pas de boutons +/-
  - **Mode vue** : comportement actuel (champs disabled, +/-, historique)

---

## T10 — Suppression d'un médicament

### Objectif
Permettre la suppression d'un médicament avec confirmation.

### Design

**`MedicineDetailViewModel`**
- Méthode `deleteMedicine()` : appelle `repo.deleteMedicine(medicineId!!, aisleId!!)` puis émet sur `navigateBack`
- N'est accessible qu'en mode vue (`medicineId != null`)

**`MedicineDetailScreen`** (mode vue uniquement)
- `TopAppBar` gagne une icône `Delete` dans ses `actions`
- État local `showDeleteDialog: Boolean` (géré dans le composable)
- Au clic sur Delete : `showDeleteDialog = true`
- `AlertDialog` :
  - Titre : "Supprimer ce médicament ?"
  - Texte : "Cette action est irréversible."
  - Bouton confirmer → `viewModel.deleteMedicine()`
  - Bouton annuler → `showDeleteDialog = false`

---

## T13 — Indicateurs de chargement et gestion d'erreurs

### Objectif
Donner un feedback visuel pendant les opérations réseau sur les écrans données.

### Design

**`MedicineViewModel` et `AisleViewModel`**
- Ajout de `private val _isLoading = MutableStateFlow(true)` + exposition `isLoading`
- Ajout de `private val _errorMessage = MutableStateFlow<String?>(null)` + exposition `errorMessage`
- Dans le `collect` du repository : `_isLoading.value = false` à la première émission ; `_errorMessage.value = message` en cas d'exception

**`MedicineScreen` et `AisleScreen`**
- Si `isLoading == true` : affiche `CircularProgressIndicator` centré
- Si `errorMessage != null` : affiche un `Text` d'erreur (rouge, centré)
- Sinon : affiche la liste

---

## T14 — Accessibilité

### Objectif
Ajouter les `contentDescription` manquants sur les éléments interactifs.

### Modifications

**`MedicineScreen` — `EmbeddedSearchBar`**
- Icône `Search` : `contentDescription = stringResource(R.string.search_cd)`
- Icône `ArrowBack` (dans la barre de recherche active) : `contentDescription = stringResource(R.string.close_search_cd)`
- Icône `Close` : `contentDescription = stringResource(R.string.clear_search_cd)`

**`MedicineScreen` — `TopAppBar`**
- Icône `MoreVert` : `contentDescription = stringResource(R.string.more_options_cd)`

**Nouvelles strings à ajouter dans `strings.xml`**
- `search_cd` : "Rechercher"
- `close_search_cd` : "Fermer la recherche"
- `clear_search_cd` : "Effacer la recherche"
- `more_options_cd` : "Plus d'options"
- `add_medicine_cd` : "Ajouter un médicament"
- `delete_medicine_cd` : "Supprimer ce médicament"

---

## Ordre d'implémentation recommandé

1. **T11** — Logout (indépendant, impact minimal)
2. **T14** — Accessibility (touches isolées, sans risque)
3. **T09** — Création médicament (modifications ViewModel + Screen + navigation)
4. **T10** — Suppression (s'appuie sur le ViewModel déjà modifié par T09)
5. **T13** — Loading states (ajouts orthogonaux aux screens existants)
