package com.coffrefort.client.utils;

import com.coffrefort.client.util.FileUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour FileUtils.
 *
 * Ces tests couvrent la logique pure (pas de JavaFX, pas d'API) —
 * pas besoin de Platform.runLater() ni de TestFX.
 *
 * Cas testés :
 * 1. getFileExtension() — extraction de l'extension
 * 2. isExtensionAllowed() — validation des extensions autorisées
 * 3. formatSize() — formatage de la taille en octets
 * 4. removeExtension() — suppression de l'extension
 * 5. getAllowedExtensionString() — liste des extensions formatée
 */
@DisplayName("FileUtils - Utilitaires de gestion des fichiers")
class FileUtilsTest {

    // ==================== getFileExtension() ====================

    @Test
    @DisplayName("Devrait extraire l'extension d'un fichier PDF")
    void testGetFileExtension_pdf() {
        assertEquals("pdf", FileUtils.getFileExtension("document.pdf"));
    }

    @Test
    @DisplayName("Devrait extraire l'extension d'un fichier image")
    void testGetFileExtension_jpg() {
        assertEquals("jpg", FileUtils.getFileExtension("photo.jpg"));
    }

    @Test
    @DisplayName("Devrait retourner l'extension en minuscules même si le nom est en majuscules")
    void testGetFileExtension_majuscules() {
        assertEquals("pdf", FileUtils.getFileExtension("DOCUMENT.PDF"));
    }

    @Test
    @DisplayName("Devrait extraire la dernière extension pour un fichier avec plusieurs points")
    void testGetFileExtension_plusieursPoints() {
        assertEquals("gz", FileUtils.getFileExtension("archive.tar.gz"));
    }

    @Test
    @DisplayName("Devrait retourner une chaîne vide si le fichier n'a pas d'extension")
    void testGetFileExtension_sansExtension() {
        assertEquals("", FileUtils.getFileExtension("fichier"));
    }

    @Test
    @DisplayName("Devrait retourner une chaîne vide si le nom est null")
    void testGetFileExtension_null() {
        assertEquals("", FileUtils.getFileExtension(null));
    }

    @Test
    @DisplayName("Devrait retourner une chaîne vide si le nom est vide")
    void testGetFileExtension_vide() {
        assertEquals("", FileUtils.getFileExtension(""));
    }

    @Test
    @DisplayName("Devrait retourner une chaîne vide si le point est en fin de nom")
    void testGetFileExtension_pointEnFin() {
        assertEquals("", FileUtils.getFileExtension("fichier."));
    }

    // ==================== isExtensionAllowed() ====================

    @ParameterizedTest
    @ValueSource(strings = {"jpg", "jpeg", "png", "webp", "pdf", "doc", "docx", "xlsx"})
    @DisplayName("Devrait accepter toutes les extensions autorisées")
    void testIsExtensionAllowed_extensionsAutorisees(String extension) {
        assertTrue(FileUtils.isExtensionAllowed(extension));
    }

    @Test
    @DisplayName("Devrait accepter une extension avec un point en préfixe")
    void testIsExtensionAllowed_avecPoint() {
        assertTrue(FileUtils.isExtensionAllowed(".pdf"));
    }

    @Test
    @DisplayName("Devrait accepter une extension en majuscules")
    void testIsExtensionAllowed_majuscules() {
        assertTrue(FileUtils.isExtensionAllowed("PDF"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"exe", "bat", "sh", "zip", "mp4", "mp3"})
    @DisplayName("Devrait refuser les extensions non autorisées")
    void testIsExtensionAllowed_extensionsRefusees(String extension) {
        assertFalse(FileUtils.isExtensionAllowed(extension));
    }

    @Test
    @DisplayName("Devrait refuser une extension null")
    void testIsExtensionAllowed_null() {
        assertFalse(FileUtils.isExtensionAllowed(null));
    }

    @Test
    @DisplayName("Devrait refuser une extension vide")
    void testIsExtensionAllowed_vide() {
        assertFalse(FileUtils.isExtensionAllowed(""));
    }

    // ==================== formatSize() ====================

    @Test
    @DisplayName("Devrait afficher en octets si la taille est inférieure à 1024")
    void testFormatSize_octets() {
        assertEquals("512 B", FileUtils.formatSize(512));
    }

    @Test
    @DisplayName("Devrait afficher en KB si la taille est entre 1024 et 1 Mo")
    void testFormatSize_kilooctets() {
        assertEquals("1,0 KB", FileUtils.formatSize(1024));
    }

    @Test
    @DisplayName("Devrait afficher en MB si la taille est entre 1 Mo et 1 Go")
    void testFormatSize_megaoctets() {
        assertEquals("1,0 MB", FileUtils.formatSize(1024 * 1024));
    }

    @Test
    @DisplayName("Devrait afficher en GB si la taille dépasse 1 Go")
    void testFormatSize_gigaoctets() {
        assertEquals("1,00 GB", FileUtils.formatSize(1024L * 1024 * 1024));
    }

    @Test
    @DisplayName("Devrait afficher 0 B pour une taille nulle")
    void testFormatSize_zero() {
        assertEquals("0 B", FileUtils.formatSize(0));
    }

    @Test
    @DisplayName("Devrait arrondir correctement en MB")
    void testFormatSize_arrondi() {
        // 1.5 MB = 1024 * 1024 * 1.5 = 1572864 bytes
        assertEquals("1,5 MB", FileUtils.formatSize(1572864));
    }

    // ==================== removeExtension() ====================

    @Test
    @DisplayName("Devrait supprimer l'extension d'un fichier simple")
    void testRemoveExtension_simple() {
        assertEquals("document", FileUtils.removeExtension("document.pdf"));
    }

    @Test
    @DisplayName("Devrait supprimer uniquement la dernière extension pour un fichier avec plusieurs points")
    void testRemoveExtension_plusieursPoints() {
        assertEquals("archive.tar", FileUtils.removeExtension("archive.tar.gz"));
    }

    @Test
    @DisplayName("Devrait retourner le nom intact si pas d'extension")
    void testRemoveExtension_sansExtension() {
        assertEquals("fichier", FileUtils.removeExtension("fichier"));
    }

    @Test
    @DisplayName("Devrait retourner null si le nom est null")
    void testRemoveExtension_null() {
        assertNull(FileUtils.removeExtension(null));
    }

    @Test
    @DisplayName("Devrait retourner une chaîne vide si le nom est vide")
    void testRemoveExtension_vide() {
        assertEquals("", FileUtils.removeExtension(""));
    }

    // ==================== getAllowedExtensionString() ====================

    @Test
    @DisplayName("Devrait retourner une chaîne contenant toutes les extensions autorisées")
    void testGetAllowedExtensionString_contientToutesLesExtensions() {
        String result = FileUtils.getAllowedExtensionString();

        assertTrue(result.contains("jpg"));
        assertTrue(result.contains("pdf"));
        assertTrue(result.contains("docx"));
        assertTrue(result.contains("xlsx"));
    }

    @Test
    @DisplayName("Devrait retourner une chaîne non vide")
    void testGetAllowedExtensionString_nonVide() {
        assertFalse(FileUtils.getAllowedExtensionString().isEmpty());
    }

    // ==================== getAllowedExtensions() ====================

    @Test
    @DisplayName("Devrait retourner une liste non vide d'extensions")
    void testGetAllowedExtensions_nonVide() {
        assertFalse(FileUtils.getAllowedExtensions().isEmpty());
    }

    @Test
    @DisplayName("Devrait retourner une liste contenant pdf et jpg")
    void testGetAllowedExtensions_contientExtensionsAttendues() {
        var extensions = FileUtils.getAllowedExtensions();

        assertTrue(extensions.contains("pdf"));
        assertTrue(extensions.contains("jpg"));
        assertTrue(extensions.contains("docx"));
        assertTrue(extensions.contains("xlsx"));
    }
}

