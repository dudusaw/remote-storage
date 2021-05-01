package org.example.client;

import javafx.application.Application;
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

    @Override
    public void start(Stage stage) throws IOException {
        NetworkService networkService = Factory.getNetworkService();
        networkService.connect("localhost", 8189);

        scene = new Scene(loadFXML("primary"));
        stage.setTitle("Client");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.setOnCloseRequest(event -> {
            Factory.getNetworkService().closeConnectionIfExists();
        });
        stage.show();
    }

    static Window getWindow() {
        return scene.getWindow();
    }

    static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public static void mainLaunch(String[] args) {
        launch();
    }

}