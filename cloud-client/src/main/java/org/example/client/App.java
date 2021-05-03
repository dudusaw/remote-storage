package org.example.client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.example.client.factory.Factory;
import org.example.client.service.NetworkService;

import java.io.IOException;

/**
 * JavaFX App
 */
public class App extends Application {

    private static Scene scene;
    private static Stage primaryStage;

    private static final String startingScene = "auth";

    @Override
    public void start(Stage stage) throws IOException {
        scene = new Scene(loadFXML(startingScene));
        App.primaryStage = stage;
        stage.setTitle("Client");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.setOnCloseRequest(event -> {
            Factory.getNetworkService().closeConnectionIfExists();
        });
        stage.show();
    }

    public static Window getWindow() {
        return scene.getWindow();
    }

    static void setNewScene(String fxml) throws IOException {
        Platform.runLater(() -> {
            try {
                scene = new Scene(loadFXML(fxml));
                primaryStage.setScene(scene);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public static void mainLaunch(String[] args) {
        launch();
    }

}