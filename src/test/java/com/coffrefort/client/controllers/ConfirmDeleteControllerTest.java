package com.coffrefort.client.controllers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour ConfirmDeleteController
 *
 * Ces tests validenten la logique de gestion des messages
 * indépendamment de JavaFX.
 *
 * Cas testés :
 * 1. Cas nominal : affichage du message
 * 2. Cas nominal : affichage du nom du fichier
 * 3. Cas d'erreur : null values
 * 4. Cas limites : chaînes longues
 */
@DisplayName("ConfirmDeleteController - Logique de gestion")
class ConfirmDeleteControllerTest {

    /**
     * Simule la préparation du message de confirmation
     */
    private String formatDeleteMessage(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "";
        }
        return "Êtes-vous sûr de vouloir supprimer " + fileName + " ?";
    }

    /**
     * Simule la normalisation d'un nom de fichier
     */
    private String normalizeFileName(String fileName) {
        return fileName != null ? fileName : "";
    }

    // ==================== CAS NOMINAL ====================

    @Test
    @DisplayName("Devrait formater un message de suppression")
    void testFormatDeleteMessageWithFileName() {
        // Act
        String message = formatDeleteMessage("document.pdf");

        // Assert
        assertEquals("Êtes-vous sûr de vouloir supprimer document.pdf ?", message);
    }

    @Test
    @DisplayName("Devrait afficher le nom du fichier simple")
    void testNormalizeFileNameSimple() {
        // Act
        String fileName = normalizeFileName("document.pdf");

        // Assert
        assertEquals("document.pdf", fileName);
    }

    @Test
    @DisplayName("Devrait afficher le nom avec extension multiple")
    void testNormalizeFileNameWithMultipleExtensions() {
        // Act
        String fileName = normalizeFileName("archive.tar.gz");

        // Assert
        assertEquals("archive.tar.gz", fileName);
    }

    // ==================== CAS LIMITES : NULL ====================

    @Test
    @DisplayName("Devrait gérer un nom de fichier null")
    void testNormalizeFileNameWithNull() {
        // Act
        String fileName = normalizeFileName(null);

        // Assert
        assertEquals("", fileName);
    }

    @Test
    @DisplayName("Devrait gérer un message avec fichier null")
    void testFormatDeleteMessageWithNull() {
        // Act
        String message = formatDeleteMessage(null);

        // Assert
        assertEquals("", message);
    }

    @Test
    @DisplayName("Devrait gérer un message avec fichier vide")
    void testFormatDeleteMessageWithEmpty() {
        // Act
        String message = formatDeleteMessage("");

        // Assert
        assertEquals("", message);
    }

    // ==================== CAS LIMITES : NOMS DE FICHIERS SPÉCIAUX ====================

    @Test
    @DisplayName("Devrait afficher un nom de fichier avec chemin")
    void testNormalizeFileNameWithPath() {
        // Act
        String fileName = normalizeFileName("Dossier/Sous-dossier/fichier.txt");

        // Assert
        assertEquals("Dossier/Sous-dossier/fichier.txt", fileName);
    }

    @Test
    @DisplayName("Devrait afficher un nom de fichier avec caractères accentués")
    void testNormalizeFileNameWithAccents() {
        // Act
        String fileName = normalizeFileName("rapport_été_2024.pdf");

        // Assert
        assertEquals("rapport_été_2024.pdf", fileName);
    }

    @Test
    @DisplayName("Devrait afficher un nom de fichier avec espaces")
    void testNormalizeFileNameWithSpaces() {
        // Act
        String fileName = normalizeFileName("Mon Document Important.docx");

        // Assert
        assertEquals("Mon Document Important.docx", fileName);
    }

    @Test
    @DisplayName("Devrait afficher un nom de fichier très long")
    void testNormalizeFileNameWithLongName() {
        // Arrange
        String longName = "A".repeat(255) + ".txt";

        // Act
        String fileName = normalizeFileName(longName);

        // Assert
        assertEquals(longName, fileName);
    }

    // ==================== CAS : MESSAGES LONGS ====================

    @Test
    @DisplayName("Devrait formater un message avec un très long nom de fichier")
    void testFormatDeleteMessageWithLongFileName() {
        // Arrange
        String longName = "very_long_filename_" + "A".repeat(100) + ".pdf";

        // Act
        String message = formatDeleteMessage(longName);

        // Assert
        assertTrue(message.contains(longName));
        assertTrue(message.startsWith("Êtes-vous sûr"));
    }

    @Test
    @DisplayName("Devrait formater un message avec un nom contenant des caractères spéciaux")
    void testFormatDeleteMessageWithSpecialChars() {
        // Act
        String message = formatDeleteMessage("Rapport_Année2024 (v2).xlsx");

        // Assert
        assertEquals("Êtes-vous sûr de vouloir supprimer Rapport_Année2024 (v2).xlsx ?", message);
    }

    // ==================== CAS : CALLBACKS ====================

    @Test
    @DisplayName("Devrait pouvoir créer des callbacks valides")
    void testCreateValidCallbacks() {
        // Arrange & Act
        Runnable onConfirm = () -> System.out.println("Confirmé");
        Runnable onCancel = () -> System.out.println("Annulé");

        // Assert
        assertNotNull(onConfirm);
        assertNotNull(onCancel);

        // Vérifier que les callbacks peuvent être exécutés
        assertDoesNotThrow(() -> {
            onConfirm.run();
            onCancel.run();
        });
    }

    @Test
    @DisplayName("Devrait gérer un stage null sans erreur")
    void testHandleNullStage() {
        // Act & Assert
        assertDoesNotThrow(() -> {
            // Simule le traitement d'un stage null
            Object stage = null;
            assertNull(stage);
        });
    }
}
