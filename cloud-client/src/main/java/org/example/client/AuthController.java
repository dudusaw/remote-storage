package org.example.client;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import org.example.client.factory.Factory;
import org.example.client.service.NetworkService;
import org.example.domain.Command;
import org.example.domain.KnownCommands;
import org.example.domain.VoidFunction;
import org.example.service.FileTransferHelperService;

import java.io.IOException;

public class AuthController {
    @FXML
    public TextField loginField;
    @FXML
    public PasswordField passwordField;
    @FXML
    public TextField hostField;
    @FXML
    public TextField portField;
    @FXML
    public TextArea responseArea;
    @FXML
    public AnchorPane anchorPane;

    private boolean connected;
    private final FileTransferHelperService fileTransferService = Factory.getFileTransferService();

    private void setupConnectionIfNotExist(VoidFunction onSuccess) {
        if (connected) {
            onSuccess.act();
            return;
        }
        appendResponseText("Connecting...");
        anchorPane.setDisable(true);

        String host = hostField.getText();
        int port = Integer.parseInt(portField.getText());

        NetworkService networkService = Factory.getNetworkService();
        networkService.connectWithCallback(host, port, success -> {
            anchorPane.setDisable(false);
            if (success) {
                connected = true;
                appendResponseText("Connect successful.");
                onSuccess.act();
            } else {
                appendResponseText("Connect failed.");
            }
        });
    }

    @FXML
    public void onLogInButton(ActionEvent actionEvent) {
        setupConnectionIfNotExist(() -> {
            String login = loginField.getText();
            String password = passwordField.getText();
            Command tryLoginCommand = new Command(KnownCommands.LoginRequest, login, password);
            fileTransferService.queueCommandCallback(KnownCommands.Ready, command -> {
                try {
                    App.setNewScene("primary");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                fileTransferService.discardNextCommandType(KnownCommands.Fail);
            });
            queuePrintMessageOnFail();
            Factory.getNetworkService().sendCommand(tryLoginCommand);
        });
    }

    @FXML
    public void OnRegisterButton(ActionEvent actionEvent) {
        setupConnectionIfNotExist(() -> {
            String login = loginField.getText();
            String password = passwordField.getText();
            Command regCommand = new Command(KnownCommands.RegisterRequest, login, password);
            fileTransferService.queueCommandCallback(KnownCommands.Ready, command -> {
                appendResponseText("Register successful.");
                fileTransferService.discardNextCommandType(KnownCommands.Fail);
            });
            queuePrintMessageOnFail();
            Factory.getNetworkService().sendCommand(regCommand);
        });
    }

    private void appendResponseText(String text) {
        Platform.runLater(() -> {
            responseArea.appendText(text + '\n');
        });
    }

    private void queuePrintMessageOnFail() {
        fileTransferService.queueCommandCallback(KnownCommands.Fail, command -> {
            appendResponseText(command.getArgs()[0]);
            fileTransferService.discardNextCommandType(KnownCommands.Ready);
        });
    }
}
