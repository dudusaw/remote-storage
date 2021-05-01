package org.example.client;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;

public class AuthController implements Initializable {
    @FXML
    public TextField loginField;
    @FXML
    public Button connectButton;
    @FXML
    public PasswordField passwordField;
    @FXML
    public TextField hostField;
    @FXML
    public TextField portField;
    @FXML
    public TextArea responseArea;
    @FXML
    public Button registerButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    @FXML
    public void OnConnectButton(ActionEvent actionEvent) {

    }

    @FXML
    public void OnRegisterButton(ActionEvent actionEvent) {

    }
}
