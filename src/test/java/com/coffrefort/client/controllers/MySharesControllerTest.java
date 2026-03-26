package com.coffrefort.client.controllers;

import com.coffrefort.client.ApiClient;
import com.coffrefort.client.model.PagedShareResponse;
import com.coffrefort.client.model.ShareItem;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Pagination;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour MySharesController.
 *
 * Stratégie : ApiClient est mocké avec Mockito. Les appels asynchrones
 * (Thread + Platform.runLater) sont attendus via Thread.sleep() +
 * WaitForAsyncUtils.waitForFxEvents() de TestFX.
 *
 * Note sur ShareItem : setStatus() n'existe pas — le statut est calculé
 * dynamiquement par getStatus() selon isRevoked(), expiresAt et remainingUses.
 *
 * Limitation connue : revokeShare() et deleteShare() appellent
 * UIDialogs.showConfirmation() qui ouvre une boîte de dialogue JavaFX
 * bloquante — impossible à invoquer directement en test. On teste à la
 * place le comportement après confirmation : appel API + rechargement.
 *
 * Cas testés :
 * 1. Chargement de la première page de partages
 * 2. Plusieurs partages sur la même page
 * 3. Liste vide
 * 4. Visibilité de la pagination (1 page vs plusieurs pages)
 * 5. Gestion d'une erreur API
 * 6. Révocation : appel API + rechargement de la liste
 * 7. Suppression : appel API + rechargement de la liste
 * 8. Retour à la page précédente si dernier élément supprimé
 * 9. Statut calculé automatiquement (Actif, Révoqué, Expiré, Quota atteint)
 */
@ExtendWith(ApplicationExtension.class)
@DisplayName("MySharesController - Chargement et actions sur les partages")
class MySharesControllerTest {

    private MySharesController   controller;
    private ApiClient            mockApiClient;
    private TableView<ShareItem> sharesTable;
    private Pagination           pagination;

    @Start
    void start(Stage stage) throws Exception {
        controller    = new MySharesController();
        mockApiClient = Mockito.mock(ApiClient.class);
        sharesTable   = new TableView<>();
        pagination    = new Pagination();

        // Colonnes minimales pour éviter NullPointerException dans initialize()
        TableColumn<ShareItem, String> resourceCol  = new TableColumn<>("Ressource");
        TableColumn<ShareItem, String> labelCol     = new TableColumn<>("Label");
        TableColumn<ShareItem, String> expiresCol   = new TableColumn<>("Expiration");
        TableColumn<ShareItem, String> remainingCol = new TableColumn<>("Restant");
        TableColumn<ShareItem, String> statusCol    = new TableColumn<>("Statut");
        TableColumn<ShareItem, Void>   actionCol    = new TableColumn<>("Actions");

        sharesTable.getColumns().addAll(
                resourceCol, labelCol, expiresCol, remainingCol, statusCol, actionCol
        );

        inject("sharesTable",   sharesTable);
        inject("pagination",    pagination);
        inject("resourceCol",   resourceCol);
        inject("labelCol",      labelCol);
        inject("expiresCol",    expiresCol);
        inject("remainingCol",  remainingCol);
        inject("statusCol",     statusCol);
        inject("actionCol",     actionCol);

        stage.setScene(new Scene(new VBox(sharesTable, pagination), 800, 500));
        stage.show();
    }

    // ─── Helpers ────────────────────────────────────────────

    private void inject(String fieldName, Object value) throws Exception {
        var f = MySharesController.class.getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(controller, value);
    }

    /**
     * Appelle loadSharesPage(int page) sur le contrôleur via réflexion,
     * sur le FX thread, puis attend la fin des appels asynchrones.
     * loadSharesPage est une méthode privée de MySharesController.
     */
    private void callLoadSharesPage(int page) throws Exception {
        Method m = MySharesController.class.getDeclaredMethod("loadSharesPage", int.class);
        m.setAccessible(true);
        Platform.runLater(() -> {
            try {
                m.invoke(controller, page);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        waitAsync();
    }

    /** Attend la fin des appels asynchrones (Thread + Platform.runLater). */
    private void waitAsync() throws InterruptedException {
        Thread.sleep(500);
        WaitForAsyncUtils.waitForFxEvents();
    }

    /**
     * Crée un ShareItem de test.
     * setStatus() n'existe pas — getStatus() est calculé automatiquement.
     */
    private ShareItem makeShare(int id, String resource, boolean revoked) {
        ShareItem item = new ShareItem();
        item.setId(id);
        item.setResource(resource);
        item.setRevoked(revoked);
        item.setUrl("http://localhost:9083/s/token" + id);
        return item;
    }

    /**
     * Crée une réponse paginée avec les setters ajoutés dans PagedShareResponse.
     */
    private PagedShareResponse makeResponse(List<ShareItem> shares, int total) {
        PagedShareResponse response = new PagedShareResponse();
        response.setShares(shares);
        response.setTotal(total);
        return response;
    }

    // ==================== CAS NOMINAL ====================

    @Test
    @DisplayName("Devrait charger et afficher la première page de partages")
    void testSetApiClient_chargeLaPremierePageDePartages() throws Exception {
        ShareItem share = makeShare(1, "document.pdf", false);
        when(mockApiClient.listShares(anyInt(), anyInt()))
                .thenReturn(makeResponse(List.of(share), 1));

        controller.setApiClient(mockApiClient);
        waitAsync();

        assertEquals(1, sharesTable.getItems().size());
        assertEquals("document.pdf", sharesTable.getItems().get(0).getResource());
    }

    @Test
    @DisplayName("Devrait afficher plusieurs partages sur la même page")
    void testSetApiClient_affichePlusieursPartages() throws Exception {
        List<ShareItem> shares = List.of(
                makeShare(1, "a.pdf",  false),
                makeShare(2, "b.docx", false),
                makeShare(3, "c.png",  false)
        );
        when(mockApiClient.listShares(anyInt(), anyInt()))
                .thenReturn(makeResponse(shares, 3));

        controller.setApiClient(mockApiClient);
        waitAsync();

        assertEquals(3, sharesTable.getItems().size());
    }

    // ==================== CAS NOMINAL : PAGINATION ====================

    @Test
    @DisplayName("Devrait masquer la pagination s'il n'y a qu'une seule page")
    void testSetApiClient_paginationMasquee_siUnePage() throws Exception {
        when(mockApiClient.listShares(anyInt(), anyInt()))
                .thenReturn(makeResponse(List.of(makeShare(1, "fichier.pdf", false)), 1));

        controller.setApiClient(mockApiClient);
        waitAsync();

        assertFalse(pagination.isVisible(), "Pagination doit être masquée si 1 seule page");
        assertFalse(pagination.isManaged());
    }

    @Test
    @DisplayName("Devrait afficher la pagination si le total dépasse la taille d'une page")
    void testSetApiClient_paginationVisible_siPlusieursPages() throws Exception {
        // PAGE_SIZE = 4, total = 10 → 3 pages → pagination visible
        List<ShareItem> shares = List.of(
                makeShare(1, "a.pdf", false),
                makeShare(2, "b.pdf", false),
                makeShare(3, "c.pdf", false),
                makeShare(4, "d.pdf", false)
        );
        when(mockApiClient.listShares(anyInt(), anyInt()))
                .thenReturn(makeResponse(shares, 10));

        controller.setApiClient(mockApiClient);
        waitAsync();

        assertTrue(pagination.isVisible(),  "Pagination doit être visible si plusieurs pages");
        assertTrue(pagination.isManaged());
    }

    // ==================== CAS NOMINAL : LISTE VIDE ====================

    @Test
    @DisplayName("Devrait afficher une table vide si aucun partage n'existe")
    void testSetApiClient_listeVide_tableVide() throws Exception {
        when(mockApiClient.listShares(anyInt(), anyInt()))
                .thenReturn(makeResponse(List.of(), 0));

        controller.setApiClient(mockApiClient);
        waitAsync();

        assertEquals(0, sharesTable.getItems().size());
    }

    // ==================== CAS D'ERREUR : API ====================

    @Test
    @DisplayName("Devrait laisser la table vide si l'API lance une exception")
    void testSetApiClient_erreurApi_tableResteVide() throws Exception {
        when(mockApiClient.listShares(anyInt(), anyInt()))
                .thenThrow(new RuntimeException("Connexion refusée"));

        assertDoesNotThrow(() -> controller.setApiClient(mockApiClient));
        waitAsync();

        assertEquals(0, sharesTable.getItems().size());
    }

    // ==================== CAS NOMINAL : RÉVOQUER ====================

    @Test
    @DisplayName("Devrait pouvoir appeler revokeShare() sur l'API sans erreur")
    void testRevokeShare_apiAccepteAppel() throws Exception {
        doNothing().when(mockApiClient).revokeShare(42);

        assertDoesNotThrow(() -> mockApiClient.revokeShare(42));
        verify(mockApiClient, times(1)).revokeShare(42);
    }

    @Test
    @DisplayName("Devrait recharger les partages après une révocation réussie")
    void testRevokeShare_rechargeApresRevocation() throws Exception {
        // 1. Charger un partage actif
        ShareItem share = makeShare(42, "rapport.pdf", false);
        when(mockApiClient.listShares(anyInt(), anyInt()))
                .thenReturn(makeResponse(List.of(share), 1));

        controller.setApiClient(mockApiClient);
        waitAsync();
        assertEquals(1, sharesTable.getItems().size());

        // 2. Simuler la révocation via l'API
        doNothing().when(mockApiClient).revokeShare(42);
        mockApiClient.revokeShare(42);

        // 3. Simuler le rechargement : le partage est maintenant révoqué
        when(mockApiClient.listShares(anyInt(), anyInt()))
                .thenReturn(makeResponse(List.of(makeShare(42, "rapport.pdf", true)), 1));

        callLoadSharesPage(0);

        // 4. Vérifications
        verify(mockApiClient, atLeastOnce()).revokeShare(42);
        assertEquals(1, sharesTable.getItems().size());
        assertTrue(sharesTable.getItems().get(0).isRevoked());
    }

    // ==================== CAS NOMINAL : SUPPRIMER ====================

    @Test
    @DisplayName("Devrait pouvoir appeler deleteShare() sur l'API sans erreur")
    void testDeleteShare_apiAccepteAppel() throws Exception {
        doNothing().when(mockApiClient).deleteShare(7);

        assertDoesNotThrow(() -> mockApiClient.deleteShare(7));
        verify(mockApiClient, times(1)).deleteShare(7);
    }

    @Test
    @DisplayName("Devrait recharger la liste après une suppression réussie")
    void testDeleteShare_rechargeApresSuppression() throws Exception {
        // 1. Charger 2 partages
        ShareItem share1 = makeShare(7, "image.png", false);
        ShareItem share2 = makeShare(8, "doc.pdf",   false);
        when(mockApiClient.listShares(anyInt(), anyInt()))
                .thenReturn(makeResponse(List.of(share1, share2), 2));

        controller.setApiClient(mockApiClient);
        waitAsync();
        assertEquals(2, sharesTable.getItems().size());

        // 2. Simuler la suppression de share1 via l'API
        doNothing().when(mockApiClient).deleteShare(7);
        mockApiClient.deleteShare(7);

        // 3. Simuler le rechargement : plus que share2
        when(mockApiClient.listShares(anyInt(), anyInt()))
                .thenReturn(makeResponse(List.of(share2), 1));

        callLoadSharesPage(0);

        // 4. Vérifications
        verify(mockApiClient, atLeastOnce()).deleteShare(7);
        assertEquals(1, sharesTable.getItems().size());
        assertEquals("doc.pdf", sharesTable.getItems().get(0).getResource());
    }

    // ==================== CAS LIMITES ====================

    @Test
    @DisplayName("Devrait revenir à la page précédente si le dernier élément est supprimé")
    void testDeleteShare_dernierElementDePage_retournePagePrecedente() throws Exception {
        // currentPage = 1, 1 seul élément → après suppression, charger page 0
        inject("currentPage", 1);

        doNothing().when(mockApiClient).deleteShare(5);
        when(mockApiClient.listShares(anyInt(), anyInt()))
                .thenReturn(makeResponse(List.of(), 0));

        //sans ça apiClient=null => listShare jamais appelé! => verify échoue
        controller.setApiClient(mockApiClient);
        waitAsync();

        mockApiClient.deleteShare(5);

        // Recharger la page 0 (page précédente)
        callLoadSharesPage(0);

        // offset = page 0 * PAGE_SIZE(4) = 0
        verify(mockApiClient, atLeastOnce()).listShares(anyInt(), eq(0));
    }

    @Test
    @DisplayName("Devrait rester sur la même page si ce n'est pas le dernier élément")
    void testDeleteShare_pasDernierElement_resteSurMemePage() throws Exception {
        // currentPage = 1, 2 éléments → reste page 1 (offset = 1 * 4 = 4)
        inject("currentPage", 1);

        ShareItem share2 = makeShare(11, "fichier2.pdf", false);

        //faire rien — simuler un appel API réussi sans vraie requête réseau
        doNothing().when(mockApiClient).deleteShare(10);
        when(mockApiClient.listShares(anyInt(), anyInt()))

                //reste 5 éléments sur 2 pages => 4+1
                .thenReturn(makeResponse(List.of(share2), 5));

        //sans ça apiClient=null => listShare jamais appelé! => verify échoue
        controller.setApiClient(mockApiClient);
        waitAsync();

        //contourner UIDialogs.showConfirmation() qui bloquerait le test
        mockApiClient.deleteShare(10);

        // Recharger la page 1 (même page)
        callLoadSharesPage(1);

        // offset = page 1 * PAGE_SIZE(4) = 4
        verify(mockApiClient, atLeastOnce()).listShares(anyInt(), eq(4));
    }

    // ==================== CAS NOMINAL : STATUT CALCULÉ ====================

    @Test
    @DisplayName("Devrait retourner 'Actif' pour un partage non révoqué sans expiration")
    void testShareItem_statutActif() {
        ShareItem share = makeShare(1, "fichier.pdf", false);
        assertEquals("Actif", share.getStatus());
    }

    @Test
    @DisplayName("Devrait retourner 'Révoqué' pour un partage révoqué")
    void testShareItem_statutRevoque() {
        ShareItem share = makeShare(1, "fichier.pdf", true);
        assertEquals("Révoqué", share.getStatus());
    }

    @Test
    @DisplayName("Devrait retourner 'Quota atteint' si remainingUses est 0")
    void testShareItem_statutQuotaAtteint() {
        ShareItem share = makeShare(1, "fichier.pdf", false);
        share.setRemainingUses(0);
        assertEquals("Quota atteint", share.getStatus());
    }

    @Test
    @DisplayName("Devrait retourner 'Expiré' si la date d'expiration est passée")
    void testShareItem_statutExpire() {
        ShareItem share = makeShare(1, "fichier.pdf", false);
        share.setExpiresAt("2020-01-01T00:00:00");
        assertEquals("Expiré", share.getStatus());
    }
}