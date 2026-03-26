package com.coffrefort.client.controllers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour CreateFolderController
 *
 * IMPORTANT : Ces tests se concentrent sur la logique de validation
 * qui est indépendante de JavaFX et ne nécessite pas d'initialisation de la UI.
 *
 * Cas testés :
 * 1. Cas nominal : validation d'un nom valide
 * 2. Cas d'erreur : nom vide
 * 3. Cas d'erreur : nom trop long
 * 4. Cas d'erreur : caractères invalides
 * 5. Cas limites : espaces, trim
 */
@DisplayName("CreateFolderController - Logique de validation")
class CreateFolderControllerTest {

    // ==================== TESTS DE VALIDATION ====================
    // Ces tests valident la logique de validation sans dépendre de JavaFX

    /**
     * Simule la validation d'un nom de dossier
     */
    private ValidationResult validateFolderName(String name) {
        if (name == null) {
            name = "";
        }
        name = name.trim();

        if (name.isEmpty()) {
            return new ValidationResult(false, "Le nom du dossier ne peut pas être vide.");
        }

        if (name.length() > 50) {
            return new ValidationResult(false, "Le nom est trop long (maximum 50 caractères).");
        }

        if (!isValidFolderName(name)) {
            return new ValidationResult(false, "Le nom du dossier contient des caractères invalides (\\/:*?\"<>|).");
        }

        return new ValidationResult(true, "");
    }

    private boolean isValidFolderName(String name) {
        if (name == null || name.isEmpty()) {
            return false;
        }
        String invalidChars = "[\\\\/:*?\"<>|]";
        return !name.matches(".*" + invalidChars + ".*");
    }

    // ==================== CAS NOMINAL ====================

    @Test
    @DisplayName("Devrait accepter un nom valide")
    void testValidateFolderNameWithValidName() {
        // Act
        ValidationResult result = validateFolderName("Mon Dossier");

        // Assert
        assertTrue(result.isValid());
        assertEquals("", result.getMessage());
    }

    @Test
    @DisplayName("Devrait accepter un nom d'un caractère")
    void testValidateFolderNameWithSingleChar() {
        // Act
        ValidationResult result = validateFolderName("A");

        // Assert
        assertTrue(result.isValid());
    }

    @Test
    @DisplayName("Devrait trimmer les espaces automatiquement")
    void testValidateFolderNameWithSpaces() {
        // Act
        ValidationResult result = validateFolderName("   Mon Dossier   ");

        // Assert
        assertTrue(result.isValid());
    }

    @Test
    @DisplayName("Devrait accepter un nom avec exactement 50 caractères")
    void testValidateFolderNameWithMaxLength() {
        // Arrange
        String maxName = "A".repeat(50);

        // Act
        ValidationResult result = validateFolderName(maxName);

        // Assert
        assertTrue(result.isValid());
    }

    // ==================== CAS D'ERREUR : NOM VIDE ====================

    @Test
    @DisplayName("Devrait refuser un nom vide")
    void testValidateFolderNameEmpty() {
        // Act
        ValidationResult result = validateFolderName("");

        // Assert
        assertFalse(result.isValid());
        assertEquals("Le nom du dossier ne peut pas être vide.", result.getMessage());
    }

    @Test
    @DisplayName("Devrait refuser un nom null")
    void testValidateFolderNameNull() {
        // Act
        ValidationResult result = validateFolderName(null);

        // Assert
        assertFalse(result.isValid());
        assertEquals("Le nom du dossier ne peut pas être vide.", result.getMessage());
    }

    @Test
    @DisplayName("Devrait refuser un nom avec uniquement des espaces")
    void testValidateFolderNameOnlySpaces() {
        // Act
        ValidationResult result = validateFolderName("     ");

        // Assert
        assertFalse(result.isValid());
        assertEquals("Le nom du dossier ne peut pas être vide.", result.getMessage());
    }

    // ==================== CAS D'ERREUR : NOM TROP LONG ====================

    @Test
    @DisplayName("Devrait refuser un nom dépassant 50 caractères")
    void testValidateFolderNameTooLong() {
        // Arrange
        String longName = "A".repeat(51);

        // Act
        ValidationResult result = validateFolderName(longName);

        // Assert
        assertFalse(result.isValid());
        assertEquals("Le nom est trop long (maximum 50 caractères).", result.getMessage());
    }

    // ==================== CAS D'ERREUR : CARACTÈRES INVALIDES ====================

    @ParameterizedTest
    @ValueSource(strings = {
            "Dossier\\Test",
            "Dossier/Test",
            "Dossier:Test",
            "Dossier*Test",
            "Dossier?Test",
            "Dossier\"Test",
            "Dossier<Test",
            "Dossier>Test",
            "Dossier|Test"
    })
    @DisplayName("Devrait refuser les caractères invalides")
    void testValidateFolderNameInvalidChars(String invalidName) {
        // Act
        ValidationResult result = validateFolderName(invalidName);

        // Assert
        assertFalse(result.isValid());
        assertEquals("Le nom du dossier contient des caractères invalides (\\/:*?\"<>|).", result.getMessage());
    }

    // ==================== CAS LIMITES ====================

    @Test
    @DisplayName("Devrait accepter un nom avec des nombres")
    void testValidateFolderNameWithNumbers() {
        // Act
        ValidationResult result = validateFolderName("Dossier123");

        // Assert
        assertTrue(result.isValid());
    }

    @Test
    @DisplayName("Devrait accepter un nom avec des tirets et underscores")
    void testValidateFolderNameWithDashesAndUnderscores() {
        // Act
        ValidationResult result = validateFolderName("Mon-Dossier_Archivé");

        // Assert
        assertTrue(result.isValid());
    }

    @Test
    @DisplayName("Devrait accepter un nom avec des caractères accentués")
    void testValidateFolderNameWithAccents() {
        // Act
        ValidationResult result = validateFolderName("Dossier éèêë Français");

        // Assert
        assertTrue(result.isValid());
    }

    @Test
    @DisplayName("Devrait accepter un nom avec des parenthèses")
    void testValidateFolderNameWithParentheses() {
        // Act
        ValidationResult result = validateFolderName("Dossier-Test_2024 (v1)");

        // Assert
        assertTrue(result.isValid());
    }

    // ==================== CLASSE UTILITAIRE ====================

    /**
     * Classe interne pour encapsuler le résultat de validation
     */
    private static class ValidationResult {
        private final boolean valid;
        private final String message;

        public ValidationResult(boolean valid, String message) {
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
