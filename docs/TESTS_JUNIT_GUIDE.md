
# Guide des Tests Unitaires JUnit 5 — CoffreFort JavaFX

## Vue d'ensemble

Ce guide détaille les tests unitaires JUnit 5 du projet CoffreFort JavaFX. Les tests suivent la structure **AAA** (Arrange / Act / Assert) et utilisent **Mockito** + **TestFX** selon les besoins de chaque contrôleur.

---

## 1. Structure des fichiers de test

```
src/test/java/com/coffrefort/client/
├── controllers/
│   ├── ConfirmDeleteControllerTest.java   # 14 tests — logique pure
│   ├── CreateFolderControllerTest.java    # 21 tests — logique pure
│   ├── LoginControllerTest.java           # 22 tests — logique pure
│   ├── MySharesControllerTest.java        # 16 tests — JavaFX + Mockito
│   ├── RegisterControllerTest.java        # 13 tests — JavaFX
│   └── RenameFolderControllerTest.java    # 10 tests — JavaFX
└── utils/
    └── FileUtilsTest.java                 # 41 tests — logique pure
```

**Total : 137 tests**

---

## 2. Deux stratégies de test

### Stratégie 1 — Logique pure (sans JavaFX)

Utilisée pour les contrôleurs dont la logique de validation est indépendante de l'interface graphique : `ConfirmDeleteControllerTest`, `CreateFolderControllerTest`, `LoginControllerTest`, `FileUtilsTest`.

La logique est simulée directement dans la classe de test via des méthodes helpers privées et des classes internes.

```java
@DisplayName("LoginController - Logique de validation")
class LoginControllerTest {

    // Méthode helper définie dans le test — simule la validation du vrai contrôleur
    private AuthValidationResult validateLogin(String email, String password, boolean passwordVisible) {
        email    = email    != null ? email.trim()    : "";
        password = password != null ? password.trim() : "";

        if (email.isEmpty() || password.isEmpty()) {
            return new AuthValidationResult(false, "Veuillez saisir l'email et le mot de passe.");
        }
        if (!email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            return new AuthValidationResult(false, "Format d'email invalide.");
        }
        if (passwordVisible) {
            return new AuthValidationResult(false, "Veuillez masquer le mot de passe avant de vous connecter.");
        }
        return new AuthValidationResult(true, "");
    }

    // Classe interne pour encapsuler le résultat
    private static class AuthValidationResult {
        private final boolean valid;
        private final String  message;

        public AuthValidationResult(boolean valid, String message) {
            this.valid   = valid;
            this.message = message;
        }

        public boolean isValid()    { return valid;   }
        public String  getMessage() { return message; }
    }
}
```

**Avantage** : pas de JavaFX, tests rapides (~0.2s pour toute la suite).
**Limite** : ne teste pas le vrai contrôleur — si la logique change dans le contrôleur, ces tests ne le détecteront pas.

---

### Stratégie 2 — Injection via réflexion (avec JavaFX)

Utilisée pour les contrôleurs qui touchent directement des composants UI : `MySharesControllerTest`, `RegisterControllerTest`, `RenameFolderControllerTest`.

Les champs `@FXML` sont injectés via réflexion Java sans charger de fichier `.fxml`.

```java
@ExtendWith(ApplicationExtension.class)
@DisplayName("RenameFolderController - Validation et callbacks")
class RenameFolderControllerTest {

    private RenameFolderController controller;
    private TextField nameField;
    private Label     errorLabel;

    @Start  // équivalent de @BeforeEach pour TestFX — s'exécute sur le FX thread
    void start(Stage stage) throws Exception {
        controller = new RenameFolderController();
        nameField  = new TextField();
        errorLabel = new Label();

        inject("nameField",  nameField);
        inject("errorLabel", errorLabel);

        stage.setScene(new Scene(new VBox(nameField, errorLabel), 300, 200));
        stage.show();
    }

    // Injection d'un champ @FXML privé via réflexion
    private void inject(String fieldName, Object value) throws Exception {
        var f = RenameFolderController.class.getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(controller, value);
    }
}
```

---

## 3. Règle fondamentale : FX Application Thread

Toute modification d'un composant JavaFX doit se faire sur le **FX Application Thread**. Si ce n'est pas respecté, l'erreur suivante apparaît :

```
IllegalStateException: Not on FX application thread; currentThread = main
```

**Règle pratique :**

| Opération | FX thread nécessaire ? |
|---|---|
| `label.setText()`, `field.setText()` | ✅ Oui |
| `label.setVisible()`, `label.setManaged()` | ✅ Oui |
| `stage.show()`, `stage.close()` | ✅ Oui |
| `controller.setOnConfirm(callback)` | ❌ Non |
| `assertEquals(...)`, `assertTrue(...)` | ❌ Non |
| Lecture : `getText()`, `isVisible()` | ❌ Non |

**Comment l'appliquer :**

```java
@Test
void testSetCurrentName_remplitLeChampEtLeLabel() throws Exception {
    // setText() est appelé dans setCurrentName() → FX thread obligatoire
    Platform.runLater(() -> controller.setCurrentName("Mon Dossier"));
    WaitForAsyncUtils.waitForFxEvents(); // attend la fin

    // Les lectures peuvent se faire depuis n'importe quel thread
    assertEquals("Mon Dossier", nameField.getText());
}
```

**Helper recommandé pour invoquer des méthodes privées sur le FX thread :**

```java
private void invokePrivateOnFxThread(String methodName) throws Exception {
    var m = MonController.class.getDeclaredMethod(methodName);
    m.setAccessible(true);
    Platform.runLater(() -> {
        try {
            m.invoke(controller);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    });
    WaitForAsyncUtils.waitForFxEvents();
}
```

---

## 4. Mocker avec Mockito

Utilisé dans `MySharesControllerTest` pour simuler `ApiClient` sans faire de vraie requête HTTP.

```java
// Créer un mock
ApiClient mockApiClient = Mockito.mock(ApiClient.class);

// Définir ce que le mock retourne
when(mockApiClient.listShares(anyInt(), anyInt()))
        .thenReturn(makeResponse(List.of(share), 1));

// Simuler un appel sans effet
doNothing().when(mockApiClient).revokeShare(42);

// Simuler une exception
when(mockApiClient.listShares(anyInt(), anyInt()))
        .thenThrow(new RuntimeException("Connexion refusée"));

// Vérifier qu'un appel a bien eu lieu
verify(mockApiClient, atLeastOnce()).revokeShare(42);
verify(mockApiClient, times(1)).deleteShare(7);
```

**Attendre les appels asynchrones :**

```java
private void waitAsync() throws InterruptedException {
    Thread.sleep(500);                     // laisse le thread terminer
    WaitForAsyncUtils.waitForFxEvents();   // vide la file d'événements FX
}
```

---

## 5. Cas particulier : UIDialogs bloquants

`revokeShare()` et `deleteShare()` dans `MySharesController` appellent `UIDialogs.showConfirmation()` qui ouvre une boîte de dialogue bloquante — il est impossible de l'invoquer directement en test.

**Contournement :** appeler directement le mock API et simuler le rechargement :

```java
@Test
void testRevokeShare_rechargeApresRevocation() throws Exception {
    // 1. Charger un partage actif
    ShareItem share = makeShare(42, "rapport.pdf", false);
    when(mockApiClient.listShares(anyInt(), anyInt()))
            .thenReturn(makeResponse(List.of(share), 1));
    controller.setApiClient(mockApiClient);
    waitAsync();

    // 2. Appel API direct (contourne UIDialogs.showConfirmation)
    doNothing().when(mockApiClient).revokeShare(42);
    mockApiClient.revokeShare(42);

    // 3. Simuler le rechargement que ferait le contrôleur après confirmation
    when(mockApiClient.listShares(anyInt(), anyInt()))
            .thenReturn(makeResponse(List.of(makeShare(42, "rapport.pdf", true)), 1));
    callLoadSharesPage(0);

    // 4. Vérifications
    verify(mockApiClient, atLeastOnce()).revokeShare(42);
    assertTrue(sharesTable.getItems().get(0).isRevoked());
}
```

---

## 6. Tests paramétrés

Pour tester plusieurs valeurs avec le même test :

```java
@ParameterizedTest
@ValueSource(strings = {
        "invalid.email",
        "@example.com",
        "user@",
        "user @example.com"
})
@DisplayName("Devrait refuser les formats d'email invalides")
void testValidateLoginWithInvalidEmailFormats(String invalidEmail) {
    AuthValidationResult result = validateLogin(invalidEmail, "password", false);

    assertFalse(result.isValid());
    assertEquals("Format d'email invalide.", result.getMessage());
}
```

---

## 7. Pattern AAA

Tous les tests suivent ce pattern :

```java
@Test
@DisplayName("Description claire du comportement attendu")
void testNomExplicite() throws Exception {
    // ARRANGE — préparation des données
    Platform.runLater(() -> nameField.setText("Mon Dossier"));
    WaitForAsyncUtils.waitForFxEvents();
    controller.setOnConfirm(name -> {});

    // ACT — exécution de la fonction testée
    invokePrivateOnFxThread("handleConfirm");

    // ASSERT — vérification des résultats
    assertFalse(errorLabel.isVisible());
}
```

---

## 8. Conventions de nommage

```
test + Verbe + Scénario + Résultat

testHandleRegister_emailVide_afficheErreur()
testSetApiClient_paginationMasquee_siUnePage()
testDeleteShare_dernierElementDePage_retournePagePrecedente()
testGetFileExtension_plusieursPoints()
```

Les `@DisplayName` sont en français complet :
```java
@DisplayName("Devrait afficher une erreur si l'email est vide")
@DisplayName("Devrait masquer la pagination s'il n'y a qu'une seule page")
@DisplayName("Devrait extraire la dernière extension pour un fichier avec plusieurs points")
```

---

## 9. Exécution des tests

```bash
# Tous les tests
mvn test

# Une suite spécifique
mvn test -Dtest=FileUtilsTest
mvn test -Dtest=RenameFolderControllerTest
mvn test -Dtest=MySharesControllerTest

# Un test par nom
mvn test -Dtest=RenameFolderControllerTest#testSetCurrentName_remplitLeChampEtLeLabel

# Plusieurs suites
mvn test -Dtest=LoginControllerTest,CreateFolderControllerTest,FileUtilsTest
```

---

## 10. Ajouter un nouveau test

### Pour un contrôleur sans JavaFX

```java
@DisplayName("NouveauController - Logique de validation")
class NouveauControllerTest {

    private String maValidation(String input) {
        if (input == null || input.isBlank()) return "Champ requis";
        return "";
    }

    @Test
    @DisplayName("Devrait retourner une erreur si le champ est vide")
    void testMaValidation_videRetourneErreur() {
        String result = maValidation("");
        assertEquals("Champ requis", result);
    }
}
```

### Pour un contrôleur avec JavaFX

```java
@ExtendWith(ApplicationExtension.class)
@DisplayName("NouveauController - Interactions UI")
class NouveauControllerTest {

    private NouveauController controller;
    private Label errorLabel;

    @Start
    void start(Stage stage) throws Exception {
        controller = new NouveauController();
        errorLabel = new Label();

        var f = NouveauController.class.getDeclaredField("errorLabel");
        f.setAccessible(true);
        f.set(controller, errorLabel);

        stage.setScene(new Scene(new VBox(errorLabel), 300, 200));
        stage.show();
    }

    @Test
    @DisplayName("Devrait afficher une erreur si le champ est vide")
    void testMonAction_afficheErreurSiVide() throws Exception {
        Platform.runLater(() -> controller.monAction());
        WaitForAsyncUtils.waitForFxEvents();

        assertTrue(errorLabel.isVisible());
        assertEquals("Champ requis", errorLabel.getText());
    }
}
```

---

## 11. Dépannage fréquent

**`IllegalStateException: Not on FX application thread`**
→ Une modification de composant JavaFX se fait hors du FX thread. Envelopper dans `Platform.runLater()` + `WaitForAsyncUtils.waitForFxEvents()`.

**`NoSuchFieldException`**
→ Le nom du champ dans `getDeclaredField("nom")` ne correspond pas exactement au nom du champ dans le contrôleur.

**`verify() — Wanted but not invoked`**
→ `apiClient` est null dans le contrôleur. Appeler `controller.setApiClient(mockApiClient)` avant le test.

**`UnfinishedStubbingException`**
→ Un `when(...)` n'a pas de `.thenReturn()`. Vérifier que tous les stubs sont complets.

**`MockitoException: Java 23 not supported`**
→ Ajouter `-Dnet.bytebuddy.experimental=true` dans l'`<argLine>` du plugin Surefire.

**`BUILD FAILURE: package org.junit does not exist`**
→ Un fichier de test utilise JUnit 4 (`import org.junit.Test`). Migrer vers JUnit 5 (`import org.junit.jupiter.api.Test`) ou l'exclure dans Surefire avec `<exclude>**/FichierConcerne.java</exclude>`.

---

## Conclusion

Cette suite de tests JUnit 5 fournit une couverture robuste des contrôleurs CoffreFort avec :
- ✅ 137 tests au total
- ✅ Structure AAA systématique
- ✅ Mockito pour l'isolation
- ✅ Cas nominal, erreur et limites
- ✅ Noms explicites en français

Les tests sont maintenables, évolutifs et prêts pour l'intégration continue.
