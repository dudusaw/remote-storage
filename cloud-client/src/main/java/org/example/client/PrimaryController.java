package org.example.client;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import org.example.client.factory.Factory;
import org.example.client.service.ControllerService;
import org.example.client.service.impl.DoubleClickChecker;
import org.example.domain.Command;
import org.example.domain.KnownCommands;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class PrimaryController implements Initializable, ControllerService {

    @FXML
    public ListView<String> listView;
    @FXML
    public Label pathLabel;
    @FXML
    public AnchorPane controlPane;
    @FXML
    private ImageView loadingImage;
    @FXML
    private MenuBar menuBar;
    @FXML
    private Label statusBar;

    private Map<String, Long> shownItems;
    private DoubleClickChecker clickChecker;
    private Path currentPath;

    private final static String backMark = "..";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Factory.setControllerService(this);

        shownItems = new HashMap<>();
        clickChecker = new DoubleClickChecker(500);
        currentPath = Paths.get("");

        loadingImage.setImage(new Image("Ellipsis-3s-200px.gif"));
        loadingImage.setVisible(false);

        setupListView();

        Factory.getNetworkService().sendCommand(new Command(KnownCommands.LocalStructureRequest));
    }

    private void setupListView() {
        listView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        listView.setOnMouseClicked(this::onMouseClicked);
        listView.setOnDragOver(event -> {
            if (event.getGestureSource() != listView && event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
            }
            event.consume();
        });
        listView.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasFiles()) {
                Factory.getFileManagerService().sendFilesToActivePath(db.getFiles(), currentPath);
                success = true;
            }
            event.setDropCompleted(success);
            event.consume();
        });
    }

    @Override
    public void setBlockedState(boolean blocked, String statusMessage) {
        runLater(() -> {
            controlPane.setDisable(blocked);
            statusBar.setText(statusMessage);
            loadingImage.setVisible(blocked);
        });
    }

    @Override
    public Path lastSelectedDir() {
        return Factory.getContextMenuService().getLastSelectedDir();
    }

    @Override
    public void updateViewLocal(String[] args) {
        clearShownItems();
        String path = args[0];
        currentPath = Paths.get(path);
        runLater(() -> pathLabel.setText("..root\\" + path));

        if (!path.isEmpty()) {
            addItem(backMark, -2);
        }

        List<Map.Entry<String, Long>> itemsToAdd = new ArrayList<>();
        for (int i = 1; i < args.length; i += 2) {
            String name = args[i];
            long length = Long.parseLong(args[i + 1]);
            itemsToAdd.add(new AbstractMap.SimpleEntry<>(name, length));
        }

        itemsToAdd.sort(PrimaryController::compareDirectoriesAndFiles);

        for (var entry : itemsToAdd) {
            addItem(entry.getKey(), entry.getValue());
        }
    }

    private static int compareDirectoriesAndFiles(Map.Entry<String, Long> o1, Map.Entry<String, Long> o2) {
        boolean is1Dir = o1.getValue() < 0;
        boolean is2Dir = o2.getValue() < 0;
        if (is1Dir == is2Dir) {
            return String.CASE_INSENSITIVE_ORDER.compare(o1.getKey(), o2.getKey());
        } else if (is1Dir) {
            return -1;
        } else {
            return 1;
        }
    }

    private void clearShownItems() {
        shownItems.clear();
        currentPath = Paths.get("");
        runLater(() -> listView.getItems().clear());
    }

    private void addItem(String item, long length) {
        shownItems.put(item, length);
        runLater(() -> listView.getItems().add(item));
    }

    private void runLater(Runnable function) {
        Platform.runLater(function);
    }

    private void onMouseClicked(MouseEvent event) {
        if (event.getButton() != MouseButton.PRIMARY) return;
        Factory.getContextMenuService().hideContextMenu();

        Node intersectedNode = event.getPickResult().getIntersectedNode();
        String clickedItemName = null;
        if (intersectedNode instanceof ListCell) {
            ListCell cell = (ListCell) intersectedNode;
            clickedItemName = cell.getText();
        } else if (intersectedNode instanceof Text) {
            Text text = (Text) intersectedNode;
            clickedItemName = text.getText();
        }
        if (clickedItemName != null && clickChecker.check(clickedItemName)) {
            onItemDoubleClick(clickedItemName);
        }
    }

    private void onItemDoubleClick(String itemName) {
        if (!isDirectory(itemName)) {
            System.out.println("This object is not a directory");
            return;
        }

        Path resultPath = currentPath;
        if (itemName.equals(backMark)) {
            if (resultPath.getParent() == null) {
                resultPath = Paths.get("");
            } else {
                resultPath = resultPath.getParent();
            }
        } else {
            resultPath = resultPath.resolve(itemName);
        }
        Command structureRequest = new Command(KnownCommands.LocalStructureRequest, resultPath.toString());
        Factory.getNetworkService().sendCommand(structureRequest);
        System.out.println(resultPath);
    }

    private boolean isDirectory(String itemName) {
        return shownItems.get(itemName) < 0;
    }

    @FXML
    public void onContextMenu(ContextMenuEvent contextMenuEvent) {
        Factory.getContextMenuService().onContextMenu(contextMenuEvent, currentPath, listView);
    }

    @Override
    public void localStructureRequest() {
        Factory.getNetworkService().sendCommand(new Command(KnownCommands.LocalStructureRequest, currentPath.toString()));
    }

    @FXML
    public void onRefreshButton(ActionEvent actionEvent) {
        localStructureRequest();
    }
}
