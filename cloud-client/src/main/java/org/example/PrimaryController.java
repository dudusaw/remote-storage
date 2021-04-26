package org.example;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;

public class PrimaryController implements Initializable {

    @FXML
    private ImageView loadingImage;
    @FXML
    private javafx.scene.control.TreeView treeViewClient;
    @FXML
    private javafx.scene.control.TreeView treeViewServer;
    @FXML
    private MenuBar menuBar;
    @FXML
    private Label statusBar;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadingImage.setImage(new Image("Ellipsis-3s-200px.gif"));
        loadingImage.setVisible(false);

//        TreeItem<String> root = new TreeItem<>("Root node");
//        root.getChildren().addAll(
//                new TreeItem<>("Item1"),
//                new TreeItem<>("Item2"),
//                new TreeItem<>("Item3")
//                );
//        TreeView.setCellFactory(param -> {
//            TreeCell<String> cell = new TreeCell<String>() {
//                @Override
//                protected void updateItem(String item, boolean empty) {
//                    super.updateItem(item, empty);
//                    Label lab = new Label(item);
//                    setGraphic(lab);
//                }
//            };
//            cell.setOnMouseClicked(event -> {
//                Node node = event.getPickResult().getIntersectedNode();
//                if (node != cell) return;
//                if (cell.getText() != null) {
//                    ContextMenu menu = new ContextMenu();
//                    menu.getItems().addAll(
//                            new MenuItem("asd1"),
//                            new MenuItem("asd2"),
//                            new MenuItem("asd3")
//                    );
//                    menu.show(event.getPickResult().getIntersectedNode(), event.getScreenX(), event.getScreenY());
//                    menu.setAutoHide(true);
//                }
//            });
//            return cell;
//        });
//        TreeView.setRoot(root);
    }
}
