# Rebonnte

Application Android de gestion de stock de médicaments pour le groupe pharmaceutique Rebonnte.

## Fonctionnalités

- Authentification par email/mot de passe (Firebase Auth)
- Consultation et gestion du stock de médicaments par rayon
- Ajout, modification et suppression de médicaments
- Historique des actions sur le stock
- Persistance des données via Firestore

## Stack technique

- Kotlin · Jetpack Compose · MVVM
- Firebase Auth · Firestore
- Hilt (injection de dépendances)

## Prérequis

- Android Studio Hedgehog ou supérieur
- JDK 17
- Un projet Firebase configuré avec Auth (email/password) et Firestore activés

## Lancer le projet

1. Cloner le dépôt
2. Depuis la console Firebase, télécharger le fichier `google-services.json` et le placer dans `app/`
3. Ouvrir le projet dans Android Studio
4. Synchroniser Gradle (`File > Sync Project with Gradle Files`)
5. Lancer l'app sur un émulateur ou un appareil physique (API 24+)
