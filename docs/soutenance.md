# Notes de soutenance — Rebonnte

Préparation à l'oral (30 min) : Product Owner Virgile évalue le résultat.

---

## Structure de la soutenance

### 1. Présentation des livrables (15 min)
- Demo de l'application fonctionnelle
- Présentation de la liste des tâches (tasks.md / Kanban)
- **2 tâches à détailler** : pourquoi ce choix, quel code produit
- Présentation de la CI (GitHub Actions)

### 2. Discussion (10 min) — Questions attendues
- "Comment avez-vous évalué les solutions possibles pour chaque tâche ?"
- "Quel choix pour répondre aux exigences du Greencode ?"
- "Comment vous êtes-vous assuré d'avoir un plan d'actions complet ?"

### 3. Debrief (5 min)

---

## Livrables à préparer

- [ ] Lien GitHub du projet
- [ ] Screenshot CI (JPEG) — build + tests OK
- [ ] PDF : liste des tâches + lien Kanban (`tasks.md` converti)
- [ ] Screenshot annoté Android Profiler (fuite mémoire corrigée — T08)
- [ ] Screenshot APK sur Firebase App Distribution (PNG/PDF)

---

## Journal de développement

*(Compléter au fur et à mesure — servira à préparer les réponses orales)*

---

### T08 — Fuite mémoire : BroadcastReceiver
- **Problème :** `BroadcastReceiver` enregistré dans `onCreate()`, jamais désenregistré → fuite mémoire sur utilisation prolongée.
- **Solution choisie :** `unregisterReceiver()` dans `onDestroy()` + `Handler(Looper.getMainLooper())`.
- **Pourquoi cette solution :** C'est la pratique standard Android. Alternative envisagée : `lifecycleScope` avec `repeatOnLifecycle`, mais `onDestroy` suffit pour un receiver de ce type.
- **Greencode :** Libérer les ressources dès qu'elles ne sont plus nécessaires réduit la consommation mémoire et CPU.
- **Statut :** [ ] À documenter avec screen Android Profiler avant/après.

---

### T04 — Refactoring MVVM
- **Problème :** `_medicines` est `public`, logique dans les composables, couplage fort via `MainActivity.mainActivity`. `Handler()` déprécié. BroadcastReceiver jamais désenregistré.
- **Solution choisie :** Encapsulation stricte (`private val _aisles`/`_medicines` + `asStateFlow()`). Repository pattern avec injection Hilt (`@HiltViewModel` + `@Inject constructor`). Suppression du `companion object mainActivity`. `MyBroadcastReceiver` devient `inner class` référençant `this@MainActivity`. `onDestroy()` appelle `unregisterReceiver()`. `Handler(Looper.getMainLooper())` remplace le `Handler()` déprécié.
- **Pourquoi :** Maintenabilité et testabilité. Sans Repository, impossible de mocker les données dans les tests unitaires. Alternative envisagée : ViewModelProvider factories manuelles — génère du boilerplate proportionnel au nombre de ViewModels.
- **Greencode :** Code structuré évite les calculs redondants et appels réseau dupliqués. Coroutines sur `Dispatchers.IO`. Receiver libéré en `onDestroy` pour réduire la consommation mémoire.
- **Statut :** [ ] À documenter avec screen Android Profiler avant/après.

---

### T06 — filterByName (perte de données)
- **Problème :** Le filtre écrase la source de données → perte définitive des données filtrées.
- **Solution choisie :** Liste source `_allMedicines` séparée, le filtre produit une vue dérivée.
- **Pourquoi :** Pattern standard pour les listes filtrables. Alternative envisagée : requête Firestore avec `.whereGreaterThanOrEqualTo()` à chaque frappe, mais trop de requêtes réseau pour une recherche en temps réel.
- **Greencode :** Éviter les appels réseau inutiles → filtrage local sur données déjà chargées.

---

### T01 — Écran Login / Register
- **Problème :** Aucune authentification. N'importe qui peut accéder aux données sans être identifié.
- **Solution choisie :** Écran Compose intégré comme route `"login"` dans le NavHost existant. `AuthRepository` wrappant `FirebaseAuth`, `AuthViewModel @HiltViewModel` avec état scellé `AuthUiState` (Idle/Loading/Success/Error). Navigation automatique vers `"aisle"` si `auth.currentUser != null` au démarrage.
- **Pourquoi :** Intégrer dans le NavHost est cohérent avec l'architecture Compose et évite une Activity supplémentaire. Alternative envisagée : `LoginActivity` séparée, mais ajoute du couplage et va à l'encontre de la migration Compose.
- **Greencode :** Pas d'appel réseau si déjà connecté (vérification `currentUser` locale). Indicateur de chargement (`CircularProgressIndicator`) pour éviter les clics multiples.

---

### T02 — Repository Aisle (Firestore)
- **Problème :** Les rayons étaient stockés uniquement en mémoire. Toutes les données disparaissaient à chaque redémarrage.
- **Solution choisie :** `AisleRepository @Singleton` avec `callbackFlow` + `addSnapshotListener` Firestore. Données triées côté Firestore (`.orderBy("name")`) exposées via `Flow<List<Aisle>>`. Listener annulé via `awaitClose { listener.remove() }`.
- **Pourquoi :** `callbackFlow` permet un listener temps réel proprement annulé. Tri côté Firestore pour éviter le calcul local. Alternative envisagée : `snapshotFlow` avec polling — moins efficace et plus de requêtes réseau.
- **Greencode :** Tri délégué à Firestore (pas de CPU client). Listener annulé quand le ViewModel est détruit (pas de requêtes orphelines).

---

### T03 — Repository Medicine (Firestore)
- **Problème :** Médicaments et historique non persistés. Données perdues entre sessions.
- **Solution choisie :** `MedicineRepository @Singleton` avec structure `aisles/{aisleId}/medicines/{medicineId}/history/{entryId}`. `updateStock` utilise `runTransaction` pour garantir l'atomicité de la mise à jour stock + écriture historique. `collectionGroup("medicines")` pour lister tous les médicaments cross-aisles.
- **Pourquoi :** `runTransaction` garantit que stock et historique sont cohérents même en cas de concurrence. Alternative envisagée : `WriteBatch` (sans lecture consistante). Filtre local pour la recherche par nom (évite une requête Firestore à chaque frappe clavier).
- **Greencode :** Opérations sur thread IO. Transaction évite les lectures/écritures redondantes. Listeners annulés proprement.

---

*(Ajouter une section par tâche terminée)*
