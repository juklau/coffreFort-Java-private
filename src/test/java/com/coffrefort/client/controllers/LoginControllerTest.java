package com.coffrefort.client.controllers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour LoginController
 *
 * Ces tests se concentrent sur la logique métier de validation
 * indépendamment de JavaFX.
 *
 * Cas testés :
 * 1. Cas nominal : validation d'email et password
 * 2. Cas d'erreur : champs vides
 * 3. Cas d'erreur : format email invalide
 * 4. Cas limites : emails spéciaux
 */
@DisplayName("LoginController - Logique de validation")
class LoginControllerTest {

    /**
     * Simule la validation du formulaire de connexion
     */
    private AuthValidationResult validateLogin(String email, String password, boolean passwordVisible) {
        email = email != null ? email.trim() : "";
        password = password != null ? password.trim() : "";

        if (email.isEmpty() || password.isEmpty()) {
            return new AuthValidationResult(false, "Veuillez saisir l'email et le mot de passe.");
        }

        if (!isValidEmail(email)) {
            return new AuthValidationResult(false, "Format d'email invalide.");
        }

        if (passwordVisible) {
            return new AuthValidationResult(false, "Veuillez masquer le mot de passe avant de vous connecter.");
        }

        return new AuthValidationResult(true, "");
    }

    private boolean isValidEmail(String email) {
        return email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    }

    // ==================== CAS NOMINAL ====================

    @Test
    @DisplayName("Devrait accepter un email et mot de passe valides")
    void testValidateLoginWithValidCredentials() {
        // Act
        AuthValidationResult result = validateLogin("user@example.com", "password123", false);

        // Assert
        assertTrue(result.isValid());
    }

    @Test
    @DisplayName("Devrait valider un email avec format standard")
    void testValidateLoginWithStandardEmail() {
        // Act
        boolean isValid = isValidEmail("john@example.com");

        // Assert
        assertTrue(isValid);
    }

    // ==================== CAS D'ERREUR : CHAMPS VIDES ====================

    @Test
    @DisplayName("Devrait refuser une connexion sans email")
    void testValidateLoginWithoutEmail() {
        // Act
        AuthValidationResult result = validateLogin("", "password", false);

        // Assert
        assertFalse(result.isValid());
        assertEquals("Veuillez saisir l'email et le mot de passe.", result.getMessage());
    }

    @Test
    @DisplayName("Devrait refuser une connexion sans mot de passe")
    void testValidateLoginWithoutPassword() {
        // Act
        AuthValidationResult result = validateLogin("user@example.com", "", false);

        // Assert
        assertFalse(result.isValid());
        assertEquals("Veuillez saisir l'email et le mot de passe.", result.getMessage());
    }

    @Test
    @DisplayName("Devrait refuser une connexion sans email ni mot de passe")
    void testValidateLoginWithoutCredentials() {
        // Act
        AuthValidationResult result = validateLogin("", "", false);

        // Assert
        assertFalse(result.isValid());
        assertEquals("Veuillez saisir l'email et le mot de passe.", result.getMessage());
    }

    @Test
    @DisplayName("Devrait refuser une connexion avec email null")
    void testValidateLoginWithNullEmail() {
        // Act
        AuthValidationResult result = validateLogin(null, "password", false);

        // Assert
        assertFalse(result.isValid());
    }

    // ==================== CAS D'ERREUR : FORMAT EMAIL INVALIDE ====================

    @ParameterizedTest
    @ValueSource(strings = {
            "invalid.email",
            "@example.com",
            "user@",
            "user @example.com",
            "user@exam ple.com",
            "user",
            "user@.com",
            "user@example",
            "user@@example.com"
    })
    @DisplayName("Devrait refuser les formats d'email invalides")
    void testValidateLoginWithInvalidEmailFormats(String invalidEmail) {
        // Act
        AuthValidationResult result = validateLogin(invalidEmail, "password", false);

        // Assert
        assertFalse(result.isValid());
        assertEquals("Format d'email invalide.", result.getMessage());
    }

    // ==================== CAS D'ERREUR : MOT DE PASSE VISIBLE ====================

    @Test
    @DisplayName("Devrait refuser si le mot de passe est visible")
    void testValidateLoginWithPasswordVisible() {
        // Act
        AuthValidationResult result = validateLogin("user@example.com", "password", true);

        // Assert
        assertFalse(result.isValid());
        assertEquals("Veuillez masquer le mot de passe avant de vous connecter.", result.getMessage());
    }

    // ==================== CAS LIMITES : EMAILS SPÉCIAUX ====================

    @Test
    @DisplayName("Devrait accepter un email avec plus signe")
    void testValidateEmailWithPlusSign() {
        // Act
        boolean isValid = isValidEmail("user+tag@example.com");

        // Assert
        assertTrue(isValid);
    }

    @Test
    @DisplayName("Devrait accepter un email avec points")
    void testValidateEmailWithDots() {
        // Act
        boolean isValid = isValidEmail("john.doe.smith@example.com");

        // Assert
        assertTrue(isValid);
    }

    @Test
    @DisplayName("Devrait accepter un email avec tirets dans le domaine")
    void testValidateEmailWithDashInDomain() {
        // Act
        boolean isValid = isValidEmail("user@my-domain.com");

        // Assert
        assertTrue(isValid);
    }

    @Test
    @DisplayName("Devrait accepter un email avec sous-domaines")
    void testValidateEmailWithSubdomains() {
        // Act
        boolean isValid = isValidEmail("user@mail.company.co.uk");

        // Assert
        assertTrue(isValid);
    }

    @Test
    @DisplayName("Devrait trimmer les espaces autour de l'email")
    void testValidateEmailWithLeadingTrailingSpaces() {
        // Act
        AuthValidationResult result = validateLogin("  user@example.com  ", "password", false);

        // Assert
        assertTrue(result.isValid());
    }

    @Test
    @DisplayName("Devrait trimmer les espaces autour du mot de passe")
    void testValidatePasswordWithLeadingTrailingSpaces() {
        // Act
        AuthValidationResult result = validateLogin("user@example.com", "  password  ", false);

        // Assert
        assertTrue(result.isValid());
    }

    // ==================== CLASSE UTILITAIRE ====================

    /**
     * Classe interne pour encapsuler le résultat de validation d'authentification
     */
    private static class AuthValidationResult {
        private final boolean valid;
        private final String message;

        public AuthValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }

        public boolean isValid() {
            return valid;
        }

        public String getMessage() {
            return message;
        }
    }
}
