package com.coffrefort.client;

import com.coffrefort.client.controllers.LoginController;
import com.coffrefort.client.controllers.MainController;
import com.coffrefort.client.controllers.RegisterController;
import com.coffrefort.client.util.SessionManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

/**
 * Mini client lourd JavaFX d'exemple pour le projet « Coffre‑fort numérique ».
 * Objectif pédagogique: fournir une base exécutable, simple à lire, sur laquelle
 * les étudiants peuvent s'appuyer pour intégrer de vrais appels REST.
 */
public class App extends Application {

    //private final ApiClient apiClient = new ApiClient();      => avant implementation de SessionManager
    private ApiClient apiClient;


    //Connexion
    @Override
    public void start(Stage stage) throws Exception {
        this.apiClient = new ApiClient();

        //config de SessionManager
        SessionManager sessionManager = SessionManager.getInstance();
        sessionManager.setApiClient(apiClient);
        sessionManager.setOnSessionExpired(() -> {

            //rediriger vers connexion
            openLogin(stage);
        });

        stage.setTitle("Coffre‑fort numérique — Mini client");
        openLogin(stage);
    }


    /**
     * ÉCRAN CONNEXION
     * @param stage
     */
    public void openLogin(Stage stage) {

        try {

            //arrêter la session quand on retourne au login ???
            SessionManager.getInstance().stopSessionMonitoring(); //=> il était dans le global try
        } catch (Exception e) {
            System.err.println("Avertissement : arrêt session échoué : " + e.getMessage());
            // on continue quand même
        }

        try {

            var url = getClass().getResource("/com/coffrefort/client/login2.fxml");
            if (url == null){
                throw new RuntimeException("login2.fxml introuvable dans les ressources");
            }
            FXMLLoader loader = new FXMLLoader(url);

            // Controller factory pour injecter ApiClient et callbacks
            loader.setControllerFactory(type -> {
                if (type == LoginController.class) {
                    LoginController controller = new LoginController();
                    controller.setApiClient(apiClient);

                    // Après connexion réussie -> tableau de bord
                    controller.setOnSuccess(() -> openMainAndClose(stage));

                    // Clique sur "S'inscrire" -> ouvrir l'écran d'inscription
                    controller.setOnGoToRegister(() -> openRegister(stage));

                    return controller;
                }

                try {
                    return type.getDeclaredConstructor().newInstance();
                } catch (Exception e) {

                    //pour dire quel controleur n'a pas réussi instancier le login
                    throw new RuntimeException("Impossible d'instancier le contrôleur : " + type.getName(), e);
                }
            });

            Parent root = loader.load();
            Scene scene = new Scene(root, 450, 650);

            stage.setScene(scene);      //avec ça le stage reste 1024 x 640
            stage.setTitle("Coffre-fort numérique — Connexion");

            //il faut redimensionner!! sinon il prend la taille de main.fxml
            stage.setWidth(450);
            stage.setHeight(650);
            stage.setResizable(false);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            throw new RuntimeException("Impossible de charger login2.fxml", e);
        } catch (Exception e) {
            throw new RuntimeException("Erreur inattendue lors de l'ouverture du login", e);
        }
    }


    /**
     * ÉCRAN INSCRIPTION => à vérif
     * @param stage
     */
    public void openRegister(Stage stage) {

        var url = getClass().getResource("/com/coffrefort/client/register.fxml");
        if (url == null) {
            throw new RuntimeException("register.fxml introuvable dans les ressources");
        }
        try {
            FXMLLoader loader = new FXMLLoader(url);

            loader.setControllerFactory(type -> {
                if (type == RegisterController.class) {
                    RegisterController controller = new RegisterController();
                    controller.setApiClient(apiClient);

                    // Après inscription réussie → retour à l'écran de login
                    controller.setOnRegisterSuccess(() -> openMainAndClose(stage));

                    // Clique sur "Se connecter" → retour à l'écran de login
                    controller.setOnGoToLogin(() -> openLogin(stage));

                    return controller;
                }
                try {
                    return type.getDeclaredConstructor().newInstance();
                } catch (Exception e) {
                    throw new RuntimeException("Impossible d'instancier le contrôleur : " + type.getName(), e);
                }
            });

            Parent root = loader.load();
            Scene scene = new Scene(root, 420, 680);

            stage.setScene(scene);  //avec ça le stage reste 1024x640
            stage.setTitle("Coffre-fort numérique — Inscription");

            //il faut redimensionner!!
            stage.setWidth(420);
            stage.setHeight(680);
            stage.setResizable(false);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            throw new RuntimeException("Impossible de charger register.fxml", e);
        } catch (Exception e) {
            throw new RuntimeException("Erreur inattendue lors de l'ouverture de l'inscription", e);
        }
    }


    /**
     * TABLEAU DE BORD
     *  * @param loginStage  => megnezni hogy megy -e!!!!!
     * Solution avec data URI pour inclure le CSS directement
     */
    private void openMainAndClose(Stage loginStage) {

        var url = getClass().getResource("/com/coffrefort/client/main.fxml");
        if (url == null) {
            throw new RuntimeException("main.fxml introuvable dans les ressources");
        }
        try {
            System.out.println("App - Chargement de main.fxml...");

            FXMLLoader loader = new FXMLLoader(url);

            // avant je n'avais pas cette partie => oader.setControllerFactory
            loader.setControllerFactory(type -> {
                if (type == MainController.class) {
                    MainController controller = new MainController();
                    controller.setApiClient(apiClient);
                    controller.setApp(this);
                    return controller;
                }
                try {
                    return type.getDeclaredConstructor().newInstance();
                } catch (Exception e) {
                    throw new RuntimeException("Impossible d'instancier : " + type.getName(), e);
                }
            });

            Parent root = loader.load();  //le controller est créé

            //récuperer le controller
            MainController controller = loader.getController();
//            controller.setApiClient(apiClient); => avant il était ici
//            controller.setApp(this);  //passer App en référence au MainController  => avant il était ici
            controller.checkAdminRole();
            //loader.setController(controller);

            System.out.println("App - Controller configuré, chargement du root...");

            //Parent root = loader.load();  //le controller est créé
            System.out.println("App - Configuration de la scène...");
//            Stage mainStage = new Stage();
//            mainStage.setTitle("Coffre‑fort — Espace personnel");

            //réutiliser le stage existant à la place de créer un nouveau
            Scene scene = new Scene(root, 1024, 640);
            loginStage.setScene(scene);

            loginStage.setTitle("Coffre‑fort — Espace personnel");

            loginStage.setWidth(1024);
            loginStage.setHeight(640);
            loginStage.setResizable(true);
            loginStage.centerOnScreen();
            loginStage.show(); // important afficher la fenetre??????? elotte commentben volt es ment

            // Fermer la fenêtre de login => avec ça je n'arriva pas ouvrir la vue de connexion
            //loginStage.close();

            //démarrer la surveillance de session après connexion réussi
            SessionManager.getInstance().startSessionMonitoring();

            System.out.println("App - Interface principale affichée");

        } catch (IOException e) {
            throw new RuntimeException("Impossible de charger main.fxml", e);
        } catch (Exception e) {
            throw new RuntimeException("Erreur inattendue lors de l'ouverture du tableau de bord", e);
        }
    }

    public static void main(String[] args) {
        launch();
    }
}
