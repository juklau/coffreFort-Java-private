# Client Lourd JavaFX - CryptoVault
## Coffre-fort Numérique Sécurisé

![Java](https://img.shields.io/badge/Java-17+-orange.svg)
![JavaFX](https://img.shields.io/badge/JavaFX-21.0.2-blue.svg)
![Maven](https://img.shields.io/badge/Maven-3.8+-red.svg)
![License](https://img.shields.io/badge/License-Academic-green.svg)

Application de bureau permettant la gestion sécurisée de fichiers chiffrés avec système de versionnage, partage contrôlé et gestion de quotas.

---

## Table des matières

- [Vue d'ensemble](#vue-densemble)
- [Fonctionnalités](#fonctionnalités)
- [Prérequis](#prérequis)
- [Installation](#installation)
- [Démarrage](#démarrage)
- [Architecture](#architecture)
- [Guide d'utilisation](#guide-dutilisation)
- [Tests](#tests)
- [CI/CD](#cicd)
- [Développement](#développement)
- [Sécurité](#sécurité)
- [Résolution de problèmes](#résolution-de-problèmes)
- [Contribution](#contribution)
- [Licence](#licence)

---

## Vue d'ensemble

Le client lourd JavaFX CryptoVault est une application de bureau multi-plateforme permettant aux utilisateurs de gérer leur espace de stockage sécurisé. L'application communique avec l'API REST backend pour effectuer toutes les opérations sur les fichiers chiffrés.

### Caractéristiques principales

- **Interface native** : Application desktop réactive avec JavaFX 21
- **Sécurité renforcée** : Gestion sécurisée des tokens JWT avec détection automatique d'expiration
- **Gestion de fichiers** : Upload/download avec barre de progression
- **Versionnage** : Historique complet des modifications de fichiers
- **Partage sécurisé** : Création de liens publics avec contrôle d'expiration et d'usage
- **Gestion de quotas** : Visualisation en temps réel de l'espace consommé

---

##  Fonctionnalités

### Authentification
-  Connexion sécurisée avec email/mot de passe
-  Gestion automatique des tokens JWT
-  **Surveillance automatique de session** (vérification toutes les minutes)
-  **Déconnexion automatique** en cas d'expiration du token
-  Déconnexion sécurisée manuelle
-  Redirection automatique vers la page de connexion

### Gestion des dossiers
-  Arborescence hiérarchique (TreeView)
-  Création de dossiers et sous-dossiers
-  Renommage de dossiers
-  Suppression de dossiers (avec confirmation)
-  Menu contextuel (clic droit)

### Gestion des fichiers
-  Liste des fichiers avec métadonnées (TableView)
-  Upload simple ou multiple avec barre de progression
-  Download de fichiers
-  Renommage et suppression
-  Support des fichiers volumineux (streaming)
-  Double-clic pour voir les détails

### Versionnage
-  Remplacement de fichiers (création automatique de nouvelles versions)
-  Historique complet des versions avec checksum SHA-256
-  Téléchargement de versions spécifiques
-  Copie du checksum dans le presse-papiers
-  Ouverture du dossier local après téléchargement

### Partages sécurisés
-  Création de liens de partage pour fichiers/dossiers
-  Configuration d'expiration (date/heure)
-  Limitation du nombre de téléchargements
-  Révocation instantanée de liens
-  Suppression de partages
-  Copie automatique du lien dans le presse-papiers
-  Affichage du statut (Actif/ Expiré/ Révoqué)

**Note** : Les sous-dossiers et les dossiers vides ne sont pas supportés dans les partages de dossiers.

### Gestion des quotas
-  Barre de progression visuelle avec code couleur
-  Alertes à 80% (orange) et 90%+ (rouge)
-  Mise à jour en temps réel après upload/suppression
-  Affichage formaté (KB, MB, GB)
-  Blocage automatique des uploads à 100%

### Administration (pour utilisateurs admin)
-  Gestion des quotas utilisateurs
-  Modification des quotas individuels
-  Suppression d'utilisateurs
-  Recherche d'utilisateurs par email
-  Vue d'ensemble de tous les utilisateurs

---

##  Prérequis

### Logiciels requis

| Logiciel | Version minimale | Version recommandée |
|----------|------------------|---------------------|
| JDK      | 17               | 21 (LTS)            |
| Maven    | 3.8.0            | 3.9.0+              |
| Git      | 2.0              | 2.40+               |
| Docker   | 20.10+           | 24.0+ (pour le backend) |

### Vérification de l'installation

```bash
# Vérifier Java
java -version
# Sortie attendue : openjdk version "17.x.x" ou supérieur

# Vérifier Maven
mvn -version
# Sortie attendue : Apache Maven 3.8.x ou supérieur

# Vérifier Docker (si backend local)
docker --version
# Sortie attendue : Docker version 20.10.x ou supérieur
```

### Dépendances principales

Le projet utilise les dépendances suivantes (gérées automatiquement par Maven) :

- JavaFX 21.0.2 : Framework d'interface graphique
- OkHttp 4.12.0 : Client HTTP pour les appels API
- Jackson 2.17.2 : Sérialisation/désérialisation JSON
- JUnit 5.10.2 : Framework de tests unitaires
- Mockito 5.11.0 : Simulation des dépendances en test
- TestFX 4.0.18 : Tests des composants JavaFX

---

##  Installation

### 1. Cloner le dépôt

```bash
git clone https://github.com/juklau/coffreFort-Java-private.git
cd coffreFort-Java-private
```

### 2. Installation des dépendances

```bash
mvn clean install
```

Cette commande :
- Télécharge toutes les dépendances nécessaires
- Compile le projet
- Package l'application

### 3. Configuration du backend

Le client nécessite un backend opérationnel. Pour démarrer le backend avec Docker :

```bash
# Dans le répertoire du backend
docker-compose up -d
```

L'API sera disponible à : `http://localhost:9083`

---

##  Démarrage

### Démarrage rapide (développement)

```bash
# Depuis le terminal
mvn clean javafx:run
```

### Démarrage depuis IntelliJ IDEA

1. Ouvrir le projet dans IntelliJ IDEA
2. Attendre que Maven synchronise les dépendances
3. Localiser la classe `com.coffrefort.client.Launcher`
4. Clic droit → Run 'Launcher.main()'

**⚠️ Note importante** : Utilisez `Launcher` et non `App` pour éviter l'erreur "JavaFX runtime components are missing".

### Premier lancement

Au premier démarrage :

1. **Écran de connexion** apparaît
2. L'URL du backend est pré-configurée : `http://localhost:9083`
3. Entrez vos identifiants :
    - Email : `votre.email@example.com`
    - Mot de passe : `VotreMotDePasse123!`
4. Cliquez sur **"Se connecter"**
5. Si la connexion réussit, le **tableau de bord principal** s'ouvre

---

## Architecture

### Structure du projet

```
src/
├── main/
│   ├── java/
│   │   └── com/
│   │       └── coffrefort/
│   │           └── client/
│   │               ├── Launcher.java              # Point d'entrée
│   │               ├── App.java                   # Application JavaFX principale
│   │               ├── ApiClient.java             # Client REST central
│   │               │
│   │               ├── config/
│   │               │   └── AppProperties.java     # Gestion configuration en mémoire
│   │               │
│   │               ├── controllers/
│   │               │   ├── LoginController.java   # Contrôleur login
│   │               │   ├── MainController.java    # Contrôleur principal
│   │               │   ├── ShareController.java   # Gestion partages
│   │               │   ├── QuotaController.java   # Admin quotas
│   │               │   ├── UploadDialogController.java  # Dialog upload
│   │               │   ├── FileDetailsController.java   # Détails fichiers
│   │               │   └── etc                          # 12 autres controllers
│   │               │
│   │               ├── model/
│   │               │   ├── NodeItem.java          # Modèle dossier
│   │               │   ├── FileEntry.java         # Modèle fichier
│   │               │   ├── ShareItem.java         # Modèle partage
│   │               │   ├── VersionEntry.java      # Modèle version
│   │               │   ├── UserQuota.java         # Modèle user
│   │               │   ├── Quota.java             # Modèle quota
│   │               │   ├── PagedFilesResponse.java    # Réponse paginée files
│   │               │   ├── PagedSharesResponse.java   # Réponse paginée shares
│   │               │   └── PagedVersionResponse.java  # Réponse paginée versions
│   │               │
│   │               └── util/
│   │                   ├── FileUtils.java         # Utilitaires fichiers
│   │                   ├── JsonUtils.java         # Utilitaires JSON
│   │                   ├── JwtUtils.java          # Décodage/validation JWT
│   │                   ├── SessionManager.java    # Gestion session (singleton)
│   │                   └── UIDialogs.java         # Dialogs (info, erreur, confirm)
│   │
│   └── resources/
│       └── com/
│           └── coffrefort/
│               └── client/
│                   ├── login2.fxml                 # Interface login
│                   ├── main.fxml                   # Interface principale
│                   ├── share.fxml                  # Dialog création partage
│                   ├── fileDetails.fxml            # Interface détails fichier
│                   ├── uploadDialog.fxml           # Dialog upload
│                   └── etc                         # 13 autres interfaces
│
└── test/
    └── java/com/coffrefort/client/
        ├── controllers/
        │   ├── ConfirmDeleteControllerTest.java   # 14 tests
        │   ├── CreateFolderControllerTest.java    # 21 tests
        │   ├── LoginControllerTest.java           # 22 tests
        │   ├── MySharesControllerTest.java        # 16 tests
        │   ├── RegisterControllerTest.java        # 13 tests
        │   └── RenameFolderControllerTest.java    # 10 tests
        └── utils/
            └── FileUtilsTest.java                 # 41 tests
```

### Pattern architectural : MVC

L'application suit le pattern **Modèle-Vue-Contrôleur** :

#### **Vue** (FXML + CSS)
- Fichiers `.fxml` définissant l'interface utilisateur
- Séparation entre présentation et logique
- Modifiable avec Scene Builder

#### **Contrôleur** (Controllers)
- Classes Java annotées `@FXML`
- Gestion des événements utilisateur
- Orchestration des appels API via `ApiClient`
- Mise à jour des vues

#### **Modèle** (Model)
- Classes représentant les entités métier (POJOs)
- Mappées depuis/vers JSON via Jackson
- Aucune logique métier (données uniquement)

### Flux de données

```
┌─────────────────┐
│   Interface     │  (FXML + CSS)
│    JavaFX       │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│   Contrôleur    │  (LoginController, MainController...)
│     (FXML)      │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│   ApiClient     │  (HttpClient Java 11 + Jackson)
│     (HTTP)      │  + SessionManager (gestion token JWT)
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│    Backend      │  (API REST Slim - Docker)
│      REST       │  http://localhost:9083
└─────────────────┘
```

---

## Guide d'utilisation

### Connexion

1. Lancer l'application
2. Saisir votre **email** et **mot de passe**
3. Cliquer sur **"Se connecter"**

**Gestion automatique de session** :
- Le token JWT est stocké en mémoire (sécurisé)
- Surveillance automatique toutes les 60 secondes
- Déconnexion automatique + alerte si le token expire
- Redirection automatique vers la page de connexion

En cas d'erreur :
- **401 Unauthorized** : Identifiants incorrects
- **500 Server Error** : Serveur indisponible

### Navigation dans les dossiers

#### Arborescence (panneau gauche)
- Cliquer sur un dossier pour afficher son contenu
- **Clic droit** → **Menu contextuel** :
    -  Nouveau sous-dossier ici
    - ✏ Renommer
    - 🗑 Supprimer (avec confirmation)
    -  Créer un partage

#### Liste des fichiers (panneau central)
- Affiche le contenu du dossier sélectionné
- **Colonnes** : Nom, Taille, Date de modification
- **Double-clic** sur un fichier → Ouvre les **détails du fichier**
- **Clic droit** → **Menu contextuel** :
    -  Télécharger
    - ️ Renommer

### Upload de fichiers

1. Sélectionner le dossier de destination dans l'arborescence
2. Cliquer sur **"Uploader"** dans la barre d'outils
3. **Sélectionner un ou plusieurs fichiers** dans le dialog
4. Observer la progression dans la barre de progression
5. Les fichiers apparaissent automatiquement dans la liste

**Vérification automatique** :
- Blocage si quota atteint (100%)
- Message d'erreur explicite avec suggestion

### Gestion des versions

#### Créer une nouvelle version (remplacer)
1. **Double-clic** sur un fichier
2. Cliquer sur **"Remplacer"** dans la fenêtre de détails
3. Sélectionner le nouveau fichier (même type obligatoire)
4. Confirmer
5. Une nouvelle version (N+1) est créée automatiquement

#### Consulter l'historique
1. **Double-clic** sur un fichier
2. Voir la **liste des versions** avec :
    - Numéro de version
    - Date de création
    - Taille
    - Checksum SHA-256

#### Actions disponibles
-  **Télécharger** une version spécifique
-  **Ouvrir le dossier** après téléchargement
-  **Copier le checksum** (clic sur bouton ou double-clic sur version)

### Création de partages

#### Partager un fichier
1. Sélectionner le fichier
2. Cliquer sur **"Partager"**
3. Configurer les paramètres :
    - **Label** : Description personnelle (optionnel)
    - **Date d'expiration** : Choisir date/heure
    - **Usages max** : Nombre de téléchargements autorisés
4. Cliquer sur **"Partager"**
5. Le lien est généré et **copié automatiquement** dans le presse-papiers

#### Partager un dossier
1. **Clic droit** sur le dossier → **"Partager ce dossier"**
2. Même configuration que pour un fichier
3. Le partage génèrera une **archive ZIP** lors du téléchargement

**⚠️ Limitations** :
- Les sous-dossiers ne sont **pas** supportés
- Les dossiers vides ne peuvent **pas** être partagés

### Gestion des partages

#### Liste "Mes partages"
1. Menu → **"Mes partages"**
2. Tableau affichant :
    -  Ressource partagée (nom)
    -  Label (description)
    -  Statut (Actif / Expiré / Révoqué)
    -  Date d'expiration
    -  Usages restants / maximum

#### Actions disponibles
-  **Copier le lien de partage** : Bouton copie dans le presse-papiers
-  **Révoquer ce partage** : Désactive immédiatement le lien (confirmation requise)
- 🗑 **Supprimer ce partage** : Supprime définitivement le partage

### Gestion du quota

#### Visualisation
- **Barre de progression** dans la barre de statut (en bas)
- Format : `X Go / Y Go`
- **Couleurs** :
    - 🟢 **Vert** : < 80%
    - 🟠 **Orange** : 80-89%
    - 🔴 **Rouge** : 90%+

#### Alertes
- **80%** : Changement de couleur (orange)
- **90%** : Changement de couleur (rouge)
- **100%** : Blocage des uploads + message explicite

#### Libérer de l'espace
1. Supprimer des fichiers inutilisés
2. Supprimer d'anciennes versions
3. Le quota se **met à jour automatiquement**

### Administration des quotas (utilisateurs admin)

**Accès** : Le bouton **"Gestion des quotas"** apparaît uniquement si l'utilisateur connecté est admin.

1. Cliquer sur **"Gestion des quotas"**
2. Tableau de tous les utilisateurs avec :
    -  Id
    -  Email
    -  Quota total
    -  Espace utilisé
    -  Pourcentage
    -  Rôle (User / Admin)

#### Actions disponibles
-  **Modifier quota** : Ajuster l'allocation d'un utilisateur
-  **Clic droit** sur utilisateur → **"Supprimer cet utilisateur"**
-  **Rechercher** un utilisateur par son email

---

## Tests

### Vue d'ensemble

**137 tests unitaires — tous passants ✅**

| Suite | Tests | Type |
|---|---|---|
| `ConfirmDeleteControllerTest` | 14 | Logique pure |
| `CreateFolderControllerTest` | 21 | Logique pure |
| `LoginControllerTest` | 22 | Logique pure |
| `MySharesControllerTest` | 16 | JavaFX + Mockito |
| `RegisterControllerTest` | 13 | JavaFX (TestFX) |
| `RenameFolderControllerTest` | 10 | JavaFX (TestFX) |
| `FileUtilsTest` | 41 | Logique pure |
| **Total** | **137** | |

### Deux stratégies de test

**Logique pure** (sans JavaFX) — pour les contrôleurs dont la validation est indépendante de l'UI. La logique est simulée directement dans le test via des méthodes helpers.

**Injection via réflexion** (avec TestFX) — pour les contrôleurs qui touchent des composants UI. Les champs `@FXML` sont injectés sans charger de fichier `.fxml`.

### Lancer les tests

```bash
# Tous les tests
mvn test

# Une suite spécifique
mvn test -Dtest=FileUtilsTest
mvn test -Dtest=MySharesControllerTest

# Un test par nom
mvn test -Dtest=RenameFolderControllerTest#testSetCurrentName_remplitLeChampEtLeLabel
```

### Résultats attendus

```
Tests run: 137, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

### Dépendances de test

```xml
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter</artifactId>
    <version>5.10.2</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.mockito</groupId>
    <artifactId>mockito-core</artifactId>
    <version>5.11.0</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.testfx</groupId>
    <artifactId>testfx-junit5</artifactId>
    <version>4.0.18</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.testfx</groupId>
    <artifactId>openjfx-monocle</artifactId>
    <version>21.0.2</version>
    <scope>test</scope>
</dependency>
```

Pour la documentation complète des tests, voir [`docs/TESTS_JUNIT_GUIDE.md`](docs/TESTS_JUNIT_GUIDE.md).

---

## CI/CD

### Workflow GitHub Actions

Le projet utilise GitHub Actions pour automatiser l'exécution des tests à chaque push et pull request.

**Fichier** : `.github/workflows/tests.yml`

```yaml
name: Tests unitaires Java

on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main, develop]
  workflow_dispatch:

jobs:
  tests:
    name: Tests unitaires (Java 23)
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          java-version: '23'
          distribution: 'temurin'
      - uses: actions/cache@v4
        with:
          path: ~/.m2/repository
          key: maven-23-${{ hashFiles('**/pom.xml') }}
      - run: mvn install -DskipTests --no-transfer-progress
      - run: mvn test --no-transfer-progress
        env:
          DISPLAY: ':99'
          _JAVA_OPTIONS: >-
            -Dtestfx.headless=true
            -Dprism.order=sw
            -Djava.awt.headless=true
            -Dnet.bytebuddy.experimental=true
```

### Déclencheurs

| Déclencheur | Quand |
|---|---|
| `push` | À chaque push sur `main` ou `develop` |
| `pull_request` | À l'ouverture d'une PR vers `main` ou `develop` |
| `workflow_dispatch` | Manuellement depuis l'onglet **Actions** de GitHub |

### Points clés pour JavaFX en CI

Sur Ubuntu GitHub Actions il n'y a pas d'écran physique — les tests JavaFX nécessitent des options spécifiques :

| Option | Rôle |
|---|---|
| `DISPLAY=':99'` | Écran virtuel pour JavaFX |
| `-Dtestfx.headless=true` | TestFX sans interface graphique |
| `-Dprism.order=sw` | Rendu logiciel (pas de GPU) |
| `-Dnet.bytebuddy.experimental=true` | Support Java 23 pour Mockito |

---

##  Développement

### Point d'entrée : `Launcher.java`

```java
package com.coffrefort.client;

import javafx.application.Application;

public class Launcher {
    public static void main(String[] args) {
        Application.launch(App.class, args);
    }
}
```

**Pourquoi Launcher ?** Évite l'erreur "JavaFX runtime components are missing" avec certains JDK.

### Gestion de la session : `SessionManager.java`

Pattern Singleton — vérifie la validité du token JWT toutes les 60 secondes et déclenche une déconnexion automatique si le token est expiré.

### Configuration : `AppProperties.java`

Gestion de la configuration en mémoire (sans fichier `.properties`) via `AppProperties.set(key, value)` et `AppProperties.get(key)`.

---

##  Sécurité

### Gestion des tokens JWT

#### Stockage sécurisé en mémoire

```java
// Dans ApiClient.java
private String authToken;

public void setAuthToken(String token) {
    this.authToken = token;
    // Stockage en mémoire uniquement (pas de persistance)
}

public void logout() {
    this.authToken = null;
    this.isAdmin = false;
    AppProperties.remove("auth.token");
    AppProperties.remove("auth.email");
    System.out.println("Déconnexion effectuée.");
}
```

**Avantages** :
-  Pas de stockage sur disque (sécurité)
-  Nettoyage automatique à la fermeture de l'application
-  Pas de risque de vol de token persistant

**Inconvénient** :
-  Nécessite une reconnexion à chaque lancement

#### Surveillance automatique d'expiration

Le `SessionManager` vérifie **automatiquement** la validité du token toutes les 60 secondes via `JwtUtils.isTokenExpired()`.

### Communication HTTP

**⚠️ Note importante** :
- Actuellement : Communication en **HTTP** (`http://localhost:9083`)
- **Production** : Passer en **HTTPS** recommandé
- **SSL/TLS** : Non implémenté pour le moment (développement local)

### Bonnes pratiques appliquées

-  **Pas de logs** de tokens ou mots de passe
-  **Pas de stockage** des mots de passe en clair
-  **Nettoyage** des données sensibles en mémoire après usage
-  **Validation** des entrées utilisateur côté client
-  **Gestion** propre des erreurs sans exposer de détails techniques
-  **Déconnexion automatique** en cas d'expiration de session

---

##  Résolution de problèmes

### "JavaFX runtime components are missing"

**Cause** : Lancement de `App.main()` directement depuis l'IDE.

**Solution** :
```bash
# Option 1 : Utiliser Launcher
Exécuter com.coffrefort.client.Launcher au lieu de App

# Option 2 : Maven
mvn clean javafx:run
```

### "Connection refused" lors des appels API

**Vérifications** :

1. **Le backend est-il démarré ?**
   ```bash
   # Vérifier que Docker tourne
   docker ps
   
   # Démarrer le backend si nécessaire
   cd /chemin/vers/backend
   docker-compose up -d
   
   # Tester l'API
   curl http://localhost:9083/
   ```

2. **L'URL de l'API est-elle correcte ?**
    - Par défaut : `http://localhost:9083`
    - Vérifier dans le code de connexion

3. **Firewall/antivirus bloque-t-il la connexion ?**
    - Autoriser Java.exe dans le pare-feu

### "401 Unauthorized" après quelques minutes

**Cause** : Token JWT expiré.

**Comportement attendu** :
- Détection automatique par `SessionManager`
- Alert "Session expirée"
- Redirection automatique vers la page de connexion

**Solution** :
- Se reconnecter avec vos identifiants
- Le token sera renouvelé automatiquement

### ProgressBar ne se met pas à jour

**Cause** : Modification de l'UI depuis un thread non-JavaFX.

**Solution** :
```java
// Utiliser Platform.runLater pour les mises à jour UI
Platform.runLater(() -> {
    progressBar.setProgress(progress);
    statusLabel.setText("Upload en cours...");
});
```

### Erreur lors du décodage JWT

**Cause** : Token JWT invalide ou corrompu.

**Vérification** :
```java
// Dans JwtUtils.java
public static boolean isTokenExpired(String token) {
    try {
        Claims claims = Jwts.parser()
            .setSigningKey(SECRET_KEY)
            .parseClaimsJws(token)
            .getBody();
        
        Date expiration = claims.getExpiration();
        return expiration.before(new Date());
    } catch (Exception e) {
        System.err.println("Erreur décodage JWT: " + e.getMessage());
        return true; // Considérer comme expiré en cas d'erreur
    }
}
```

### Les tests Maven échouent

```bash
# Nettoyer et réessayer
mvn clean test

# Ignorer les tests pour le build
mvn clean install -DskipTests
```

---

##  Contribution

### Workflow Git

```bash
# Créer une branche feature
git checkout -b feature/nouvelle-fonctionnalite

# Développer et commiter
git add .
git commit -m "feat: ajout de la fonctionnalité X"

# Pousser et créer une Pull Request
git push origin feature/nouvelle-fonctionnalite
```

### Convention de commits

Suivre la convention [Conventional Commits](https://www.conventionalcommits.org/) :

- `feat:` Nouvelle fonctionnalité
- `fix:` Correction de bug
- `docs:` Documentation
- `style:` Formatage, point-virgules manquants, etc.
- `refactor:` Refactoring de code
- `test:` Ajout de tests
- `chore:` Tâches de maintenance

---

##  Licence

Ce projet est développé dans un cadre pédagogique.

**Utilisation académique uniquement**.

---

##  Support

Pour toute question ou problème :

- **Issues GitHub** : [https://github.com/juklau/coffreFort-Java-private/issues](https://github.com/juklau/coffreFort-Java-private/issues)
- **Documentation backend** : [Voir le README du backend](https://github.com/juklau/coffreFort-backEnd-private.git)
- **Documentation tests** : Voir [`docs/TESTS_JUNIT_GUIDE.md`](docs/TESTS_JUNIT_GUIDE.md)

---

## Créateurs

**Klaudia Juhasz**
**Denys Lyulchak**

---

##  Notes de version

**Version** : 1.0.0  
**Dernière mise à jour** : 12 février 2026

### Fonctionnalités implémentées
- Authentification avec JWT + surveillance automatique de session
- Gestion des dossiers (CRUD complet)
- Gestion des fichiers (upload, download, suppression)
- Versionnage de fichiers
- Partages sécurisés (création, révocation, suppression)
- Gestion des quotas (visualisation, alertes)
- Administration (modification quotas, suppression users)
- 137 tests unitaires (JUnit 5 + Mockito + TestFX)
- CI/CD GitHub Actions

### En développement / À implémenter

####  Sécurité
-  Communication HTTPS (production)
-  Politique de mot de passe renforcée (complexité, longueur)
-  Fonctionnalité "Mot de passe oublié" (réinitialisation par email)
-  Refresh token (renouvellement automatique de session)

####  Configuration et déploiement
-  Fichier config.properties externe
-  Déploiement avec jpackage (exécutables natifs Windows/macOS/Linux)

####  Fonctionnalités utilisateur
-  Déplacer fichiers et dossiers (drag & drop ou menu contextuel)
-  Placer des fichiers à la racine (sans dossier parent)
-  Amélioration barre de progression (annulation, vitesse en temps réel)
-  Implémentation "Mon historique" (consultation des actions passées)
-  Page "Mentions légales" (accessible depuis le menu Aide)
-  Page "À propos" avec informations sur l'application

####  Internationalisation
-  Support multilingue (FR/EN)


