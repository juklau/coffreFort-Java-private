package com.coffrefort.client.controllers;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour RegisterController.
 *
 * Stratégie : les champs @FXML sont injectés via réflexion sans charger
 * de fichier .fxml — les composants JavaFX sont instanciés directement
 * en mémoire. Aucun appel API réel n'est effectué.
 *
 * Cas testés :
 * 1. Validation du formulaire (email, password, confirmation)
 * 2. Gestion des labels d'erreur et de succès
 * 3. Callbacks onGoToLogin
 * 4. Cas limites (null, label null)
 */
@ExtendWith(ApplicationExtension.class)
@DisplayName("RegisterController - Validation du formulaire")
class RegisterControllerTest {

    // mot de passe valide réutilisé dans tous les tests qui ne testent pas le password
    private static final String VALID_PASSWORD = "ValidPass123!";

    private RegisterController controller;

    private TextField     emailField;
    private PasswordField passwordField;
    private PasswordField confirmPasswordField;
    private TextField     passwordVisibleField;
    private TextField     confirmPasswordVisibleField;
    private CheckBox      showPasswordCheckBox;
    private Label         errorLabel;
    private Label         successLabel;
    private Label         emailError;
    private Label         passwordError;
    private Label         confirmPasswordError;
    private Label         statusLabel;
    private Button        registerButton;
    private GridPane      rootPane;

    @Start
    void start(Stage stage) throws Exception {
        controller = new RegisterController();

        emailField                  = new TextField();
        passwordField               = new PasswordField();
        confirmPasswordField        = new PasswordField();
        passwordVisibleField        = new TextField();
        confirmPasswordVisibleField = new TextField();
        showPasswordCheckBox        = new CheckBox();
        errorLabel                  = new Label();
        successLabel                = new Label();
        emailError                  = new Label();
        passwordError               = new Label();
        confirmPasswordError        = new Label();
        statusLabel                 = new Label();
        registerButton              = new Button("S'inscrire");
        rootPane                    = new GridPane();

        inject("emailField1",                  emailField);
        inject("passwordField1",               passwordField);
        inject("confirmPasswordField1",        confirmPasswordField);
        inject("passwordVisibleField1",        passwordVisibleField);
        inject("confirmPasswordVisibleField1", confirmPasswordVisibleField);
        inject("loginSelectShowPassword",      showPasswordCheckBox);
        inject("errorLabel1",                  errorLabel);
        inject("successLabel1",                successLabel);
        inject("emailError1",                  emailError);
        inject("passwordError1",               passwordError);
        inject("confirmPasswordError1",        confirmPasswordError);
        inject("statusLabel1",                 statusLabel);
        inject("registerButton1",              registerButton);
        inject("rootPane",                     rootPane);

        stage.setScene(new Scene(rootPane, 400, 300));
        stage.show();
    }

    // ─── Helpers ────────────────────────────────────────────

    private void inject(String fieldName, Object value) throws Exception {
        var f = RegisterController.class.getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(controller, value);
    }

    private void setFields(String email, String password, String confirm) {
        emailField.setText(email);
        passwordField.setText(password);
        confirmPasswordField.setText(confirm);
    }

    // ==================== CAS NOMINAUX ====================

    @Test
    @DisplayName("Devrait afficher une erreur si l'email est vide")
    void testHandleRegister_emailVide_afficheErreur() {
        // mot de passe valide — seul l'email doit échouer
        setFields("", VALID_PASSWORD, VALID_PASSWORD);

        controller.handleRegister();

        assertEquals("Veuillez saisir votre email.", errorLabel.getText());
        assertTrue(errorLabel.isVisible());
    }

    @Test
    @DisplayName("Devrait afficher un message de succès via showSuccess()")
    void testShowSuccess_afficheMessageDansSuccessLabel() {
        controller.showSuccess("Bienvenue !");

        assertEquals("Bienvenue !", successLabel.getText());
        assertTrue(successLabel.isVisible());
        assertTrue(successLabel.isManaged());
    }

    @Test
    @DisplayName("Devrait afficher un message d'erreur via showError()")
    void testShowError_afficheMessageDansErrorLabel() {
        controller.showError("Erreur de connexion.");

        assertEquals("Erreur de connexion.", errorLabel.getText());
        assertTrue(errorLabel.isVisible());
        assertTrue(errorLabel.isManaged());
    }

    // ==================== VALIDATION EMAIL ====================

    @Test
    @DisplayName("Devrait afficher une erreur si le format de l'email est invalide")
    void testHandleRegister_emailFormatInvalide_afficheErreur() {
        // mot de passe valide — seul l'email doit échouer
        setFields("emailsansarobase", VALID_PASSWORD, VALID_PASSWORD);

        controller.handleRegister();

        assertEquals("Format d'email invalide.", errorLabel.getText());
        assertTrue(errorLabel.isVisible());
    }

    @Test
    @DisplayName("Devrait afficher une erreur si l'email dépasse 255 caractères")
    void testHandleRegister_emailTropLong_afficheErreur() {
        String emailTropLong = "a".repeat(250) + "@test.fr";
        setFields(emailTropLong, VALID_PASSWORD, VALID_PASSWORD);

        controller.handleRegister();

        assertEquals("L'email est trop long (maximum 255 caractères).", errorLabel.getText());
        assertTrue(errorLabel.isVisible());
    }

    // ==================== VALIDATION MOT DE PASSE ====================

    @Test
    @DisplayName("Devrait afficher une erreur si le mot de passe est vide")
    void testHandleRegister_passwordVide_afficheErreur() {
        setFields("test@test.fr", "", "");

        controller.handleRegister();

        assertEquals("Le mot de passe est obligatoire.", errorLabel.getText());
        assertTrue(errorLabel.isVisible());
    }

    @Test
    @DisplayName("Devrait afficher une erreur si le mot de passe est trop court (< 12 caractères)")
    void testHandleRegister_passwordTropCourt_afficheErreur() {
        // 11 chars, sinon déclenche d'autres règles → on teste juste la longueur
        setFields("test@test.fr", "Short1!", "Short1!");

        controller.handleRegister();

        assertEquals("Le mot de passe est trop court (minimum 12 caractères).", errorLabel.getText());
        assertTrue(errorLabel.isVisible());
    }

    @Test
    @DisplayName("Devrait afficher une erreur si le mot de passe dépasse 128 caractères")
    void testHandleRegister_passwordTropLong_afficheErreur() {
        // 129 chars avec majuscule, minuscule, chiffre, spécial
        String trop_long = "A1!" + "a".repeat(126);
        setFields("test@test.fr", trop_long, trop_long);

        controller.handleRegister();

        assertEquals("Le mot de passe ne peut pas dépasser 128 caractères.", errorLabel.getText());
        assertTrue(errorLabel.isVisible());
    }

    @Test
    @DisplayName("Devrait afficher une erreur si le mot de passe n'a pas de majuscule")
    void testHandleRegister_passwordSansMajuscule_afficheErreur() {
        String sansMajuscule = "lowercase123!";
        setFields("test@test.fr", sansMajuscule, sansMajuscule);

        controller.handleRegister();

        assertEquals("Le mot de passe doit contenir au moins une lettre majuscule.", errorLabel.getText());
        assertTrue(errorLabel.isVisible());
    }

    @Test
    @DisplayName("Devrait afficher une erreur si le mot de passe n'a pas de minuscule")
    void testHandleRegister_passwordSansMinuscule_afficheErreur() {
        String sansMinuscule = "UPPERCASE123!";
        setFields("test@test.fr", sansMinuscule, sansMinuscule);

        controller.handleRegister();

        assertEquals("Le mot de passe doit contenir au moins une lettre minuscule.", errorLabel.getText());
        assertTrue(errorLabel.isVisible());
    }

    @Test
    @DisplayName("Devrait afficher une erreur si le mot de passe n'a pas de chiffre")
    void testHandleRegister_passwordSansChiffre_afficheErreur() {
        String sansChiffre = "PasswordOnly!";
        setFields("test@test.fr", sansChiffre, sansChiffre);

        controller.handleRegister();

        assertEquals("Le mot de passe doit contenir au moins un chiffre.", errorLabel.getText());
        assertTrue(errorLabel.isVisible());
    }

    @Test
    @DisplayName("Devrait afficher une erreur si le mot de passe n'a pas de caractère spécial")
    void testHandleRegister_passwordSansSpecial_afficheErreur() {
        String sansSpecial = "Password12345";
        setFields("test@test.fr", sansSpecial, sansSpecial);

        controller.handleRegister();

        assertEquals("Le mot de passe doit contenir au moins un caractère spécial (!@#$%^&*...).", errorLabel.getText());
        assertTrue(errorLabel.isVisible());
    }

    // ==================== VALIDATION CONFIRMATION ====================

    @Test
    @DisplayName("Devrait afficher une erreur si la confirmation est vide")
    void testHandleRegister_confirmPasswordVide_afficheErreur() {
        // mot de passe valide, confirmation vide
        setFields("test@test.fr", VALID_PASSWORD, "");

        controller.handleRegister();

        assertEquals("Veuillez confirmer le mot de passe.", errorLabel.getText());
        assertTrue(errorLabel.isVisible());
    }

    @Test
    @DisplayName("Devrait afficher une erreur si les mots de passe ne correspondent pas")
    void testHandleRegister_passwordsNonIdentiques_afficheErreur() {
        setFields("test@test.fr", VALID_PASSWORD, "DifferentPass123!");

        controller.handleRegister();

        assertEquals("Les mots de passe ne correspondent pas.", errorLabel.getText());
        assertTrue(errorLabel.isVisible());
    }

    @Test
    @DisplayName("Devrait afficher une erreur si la case 'afficher mot de passe' est cochée")
    void testHandleRegister_showPasswordCoche_afficheErreur() {
        // mot de passe valide — seule la case cochée doit échouer
        setFields("test@test.fr", VALID_PASSWORD, VALID_PASSWORD);
        showPasswordCheckBox.setSelected(true);

        controller.handleRegister();

        assertEquals("Veuillez masquer le mot de passe avant de vous inscrire.", errorLabel.getText());
        assertTrue(errorLabel.isVisible());
    }

    // ==================== CAS LIMITES ====================

    @Test
    @DisplayName("Devrait masquer tous les labels après clearAllErrors()")
    void testClearAllErrors_masqueTousLesLabels() {
        errorLabel.setText("Erreur test");
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);

        controller.clearAllErrors();

        assertFalse(errorLabel.isVisible());
        assertFalse(errorLabel.isManaged());
        assertEquals("", errorLabel.getText());
    }

    @Test
    @DisplayName("Devrait appeler le callback onGoToLogin quand handleGoToLogin() est appelé")
    void testSetOnGoToLogin_callbackAppele() {
        AtomicBoolean called = new AtomicBoolean(false);
        controller.setOnGoToLogin(() -> called.set(true));

        controller.handleGoToLogin();

        assertTrue(called.get(), "Le callback onGoToLogin aurait dû être appelé");
    }

    @Test
    @DisplayName("Devrait ne pas lancer d'exception si le callback onGoToLogin est null")
    void testSetOnGoToLogin_sansCallback_neLancePasException() {
        controller.setOnGoToLogin(null);

        assertDoesNotThrow(() -> controller.handleGoToLogin());
    }

    @Test
    @DisplayName("Devrait ne pas lancer d'exception si hideLabel() reçoit null")
    void testHideLabel_labelNull_neLancePasException() {
        assertDoesNotThrow(() -> controller.hideLabel(null));
    }
}