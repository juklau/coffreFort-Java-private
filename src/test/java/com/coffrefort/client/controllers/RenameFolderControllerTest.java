package com.coffrefort.client.controllers;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour RenameFolderController.
 *
 * Stratégie : les champs @FXML sont injectés via réflexion sans charger
 * de fichier .fxml. Les méthodes privées sont invoquées via réflexion.
 * Aucun appel API — ce contrôleur est purement UI.
 *
 * Règle JavaFX : toute modification de composant UI (setText, setVisible...)
 * doit se faire sur le FX Application Thread via Platform.runLater().
 * Les lectures (getText, isVisible...) et la configuration de callbacks
 * peuvent se faire depuis n'importe quel thread.
 *
 * Cas testés :
 * 1. Pré-remplissage du champ de saisie (setCurrentName)
 * 2. Validation du nom (vide, espaces seuls)
 * 3. Callback onConfirm appelé avec le bon nom
 * 4. Masquage de l'erreur après validation réussie
 * 5. Fermeture du stage (handleCancel, close)
 * 6. Cas limites (null stage, null callback)
 */
@ExtendWith(ApplicationExtension.class)
@DisplayName("RenameFolderController - Validation et callbacks")
class RenameFolderControllerTest {

    private RenameFolderController controller;
    private TextField nameField;
    private Label     currentNameLabel;
    private Label     errorLabel;
    private Stage     dialogStage;

    @Start
    void start(Stage stage) throws Exception {
        controller       = new RenameFolderController();
        nameField        = new TextField();
        currentNameLabel = new Label();
        errorLabel       = new Label();
        dialogStage      = new Stage();

        inject("nameField",        nameField);
        inject("currentNameLabel", currentNameLabel);
        inject("errorLabel",       errorLabel);

        controller.setStage(dialogStage);

        stage.setScene(new Scene(new VBox(nameField, currentNameLabel, errorLabel), 300, 200));
        stage.show();
    }

    // ─── Helpers ────────────────────────────────────────────

    private void inject(String fieldName, Object value) throws Exception {
        var f = RenameFolderController.class.getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(controller, value);
    }

    /** Invoque une méthode privée sur le FX thread et attend la fin. */
    private void invokePrivateOnFxThread(String methodName) throws Exception {
        var m = RenameFolderController.class.getDeclaredMethod(methodName);
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

    // ==================== CAS NOMINAL ====================

    @Test
    @DisplayName("Devrait pré-remplir le champ et le label avec le nom actuel")
    void testSetCurrentName_remplitLeChampEtLeLabel() throws Exception {
        // setCurrentName() appelle setText() + requestFocus() → FX thread obligatoire
        Platform.runLater(() -> controller.setCurrentName("Mon Dossier"));
        WaitForAsyncUtils.waitForFxEvents();

        assertEquals("Mon Dossier", currentNameLabel.getText());
        assertEquals("Mon Dossier", nameField.getText());
    }

    @Test
    @DisplayName("Devrait appeler le callback avec le nouveau nom saisi")
    void testHandleConfirm_nomValide_appeleCallback() throws Exception {
        // setText() → FX thread
        Platform.runLater(() -> nameField.setText("Nouveau nom"));
        WaitForAsyncUtils.waitForFxEvents();

        // setOnConfirm() ne touche pas l'UI → pas besoin de FX thread
        AtomicReference<String> received = new AtomicReference<>("");
        controller.setOnConfirm(received::set);

        // handleConfirm() appelle hideError() (setVisible/setManaged) → FX thread
        invokePrivateOnFxThread("handleConfirm");

        assertEquals("Nouveau nom", received.get());
    }

    @Test
    @DisplayName("Devrait masquer l'erreur si le nom saisi est valide")
    void testHandleConfirm_nomValide_masqueLErreur() throws Exception {
        // Préparer l'état initial sur le FX thread
        Platform.runLater(() -> {
            nameField.setText("Dossier valide");
            errorLabel.setText("Erreur précédente");
            errorLabel.setVisible(true);
            errorLabel.setManaged(true);
        });
        WaitForAsyncUtils.waitForFxEvents();

        controller.setOnConfirm(name -> {});

        // handleConfirm() → FX thread
        invokePrivateOnFxThread("handleConfirm");

        assertFalse(errorLabel.isVisible());
        assertFalse(errorLabel.isManaged());
    }

    // ==================== CAS D'ERREUR : VALIDATION NOM ====================

    @Test
    @DisplayName("Devrait afficher une erreur si le nom est vide")
    void testHandleConfirm_nomVide_afficheErreur() throws Exception {
        // setText() → FX thread
        Platform.runLater(() -> nameField.setText(""));
        WaitForAsyncUtils.waitForFxEvents();

        // handleConfirm() → FX thread
        invokePrivateOnFxThread("handleConfirm");

        assertEquals("Le nom ne peut pas être vide", errorLabel.getText());
        assertTrue(errorLabel.isVisible());
        assertTrue(errorLabel.isManaged());
    }

    @Test
    @DisplayName("Devrait afficher une erreur si le nom contient uniquement des espaces")
    void testHandleConfirm_nomEspacesSeuls_afficheErreur() throws Exception {
        Platform.runLater(() -> nameField.setText("   "));
        WaitForAsyncUtils.waitForFxEvents();

        invokePrivateOnFxThread("handleConfirm");

        assertEquals("Le nom ne peut pas être vide", errorLabel.getText());
        assertTrue(errorLabel.isVisible());
    }

    // ==================== CAS D'ERREUR : FERMETURE ====================

    @Test
    @DisplayName("Devrait fermer le dialogStage quand handleCancel() est appelé")
    void testHandleCancel_fermeLeDialogStage() throws Exception {
        // show() → FX thread
        Platform.runLater(() -> dialogStage.show());
        WaitForAsyncUtils.waitForFxEvents();

        assertTrue(dialogStage.isShowing());

        // handleCancel() appelle dialogStage.close() → FX thread
        invokePrivateOnFxThread("handleCancel");

        assertFalse(dialogStage.isShowing());
    }

    @Test
    @DisplayName("Devrait fermer le dialogStage quand close() est appelé")
    void testClose_fermeLeDialogStage() throws Exception {
        Platform.runLater(() -> dialogStage.show());
        WaitForAsyncUtils.waitForFxEvents();

        assertTrue(dialogStage.isShowing());

        // close() appelle dialogStage.close() → FX thread
        Platform.runLater(() -> controller.close());
        WaitForAsyncUtils.waitForFxEvents();

        assertFalse(dialogStage.isShowing());
    }

    // ==================== CAS LIMITES ====================

    @Test
    @DisplayName("Devrait ne pas lancer d'exception si le callback est null")
    void testHandleConfirm_sansCallback_neLancePasException() throws Exception {
        Platform.runLater(() -> nameField.setText("Dossier valide"));
        WaitForAsyncUtils.waitForFxEvents();

        controller.setOnConfirm(null);

        AtomicBoolean threw = new AtomicBoolean(false);
        Platform.runLater(() -> {
            try {
                var m = RenameFolderController.class.getDeclaredMethod("handleConfirm");
                m.setAccessible(true);
                m.invoke(controller);
            } catch (Exception e) {
                threw.set(true);
            }
        });
        WaitForAsyncUtils.waitForFxEvents();

        assertFalse(threw.get(), "Aucune exception ne devrait être lancée si le callback est null");
    }

    @Test
    @DisplayName("Devrait ne pas lancer d'exception si le stage est null lors du cancel")
    void testHandleCancel_sansStage_neLancePasException() throws Exception {
        controller.setStage(null);

        AtomicBoolean threw = new AtomicBoolean(false);
        Platform.runLater(() -> {
            try {
                var m = RenameFolderController.class.getDeclaredMethod("handleCancel");
                m.setAccessible(true);
                m.invoke(controller);
            } catch (Exception e) {
                threw.set(true);
            }
        });
        WaitForAsyncUtils.waitForFxEvents();

        assertFalse(threw.get(), "Aucune exception ne devrait être lancée si le stage est null");
    }

    @Test
    @DisplayName("Devrait ne pas lancer d'exception si le stage est null lors du close")
    void testClose_sansStage_neLancePasException() throws Exception {
        controller.setStage(null);

        AtomicBoolean threw = new AtomicBoolean(false);
        Platform.runLater(() -> {
            try {
                controller.close();
            } catch (Exception e) {
                threw.set(true);
            }
        });
        WaitForAsyncUtils.waitForFxEvents();

        assertFalse(threw.get(), "Aucune exception ne devrait être lancée si le stage est null");
    }
}