package org.example.client;

import io.netty.handler.stream.ChunkedFile;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import org.example.domain.Command;
import org.example.domain.CommonUtil;
import org.example.domain.KnownCommands;
import org.example.client.factory.Factory;
import org.example.client.service.*;
import org.example.service.FileTransferHelperService;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
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
    private ContextMenu lastOpenedMenu;
    private Path lastSelectedDir = Path.of("");

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

        NetworkService networkService = Factory.getNetworkService();
        networkService.sendCommand(new Command(KnownCommands.LocalStructureRequest));
    }

    private void setupListView() {
        listView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        listView.setOnMouseClicked(this::onMouseClicked);
        listView.setOnDragOver(new EventHandler<DragEvent>() {
            @Override
            public void handle(DragEvent event) {
                if (event.getGestureSource() != listView && event.getDragboard().hasFiles()) {
                    event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
                }
                event.consume();
            }
        });
        listView.setOnDragDropped(new EventHandler<DragEvent>() {
            @Override
            public void handle(DragEvent event) {
                Dragboard db = event.getDragboard();
                boolean success = false;
                if (db.hasFiles()) {
                    sendFilesToActivePath(db.getFiles());
                    success = true;
                }
                event.setDropCompleted(success);
                event.consume();
            }
        });
    }

    private void sendFilesToActivePath(List<File> files) {
        try {
            FileTransferHelperService transferHelperService = Factory.getFileTransferService();
            NetworkService networkService = Factory.getNetworkService();
            List<String> argsList = new ArrayList<>();
            List<ChunkedFile> chunkedFiles = new ArrayList<>();
            for (File file : files) {
                if (file.isDirectory()) {
                    Files.walkFileTree(file.toPath(), new SimpleFileVisitor<>() {
                        @Override
                        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                            // Create empty directories first, non empty ones will be created with the actual files
                            Path fileRelativePath = currentPath.resolve(file.toPath().getParent().relativize(dir).normalize());
                            if (CommonUtil.isDirectoryEmpty(dir)) {
                                Command createDir = new Command(KnownCommands.CreateDirectory, fileRelativePath.toString());
                                networkService.sendCommand(createDir);
                            }
                            return FileVisitResult.CONTINUE;
                        }

                        @Override
                        public FileVisitResult visitFile(Path visitedFile, BasicFileAttributes attrs) throws IOException {
                            Path fileRelativePath = currentPath.resolve(file.toPath().getParent().relativize(visitedFile).normalize());
                            addPathToLists(argsList, chunkedFiles, fileRelativePath, new File(visitedFile.toString()));
                            return FileVisitResult.CONTINUE;
                        }
                    });
                } else {
                    addPathToLists(argsList, chunkedFiles, currentPath.resolve(file.getName()), file);
                }
            }
            if (argsList.isEmpty()) {
                setBlockedState(false, "Nothing to send.");
                return;
            }
            Command transf = new Command(KnownCommands.FileTransfer, argsList);
            networkService.sendCommand(transf);
            setBlockedState(true, "Sending files...");
            transferHelperService.queueReadyCallback(() -> {
                Factory.getPipelineManager().setup(PipelineSetup.FILE.handlers);
                for (ChunkedFile chunkedFile : chunkedFiles) {
                    networkService.sendFile(chunkedFile);
                }
                Factory.getPipelineManager().setup(PipelineSetup.COMMAND.handlers);
            });
            transferHelperService.queueReadyCallback(() -> {
                setBlockedState(false, "Sending complete.");
                localStructureRequest();
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addPathToLists(List<String> argsList, List<ChunkedFile> chunkedFiles, Path relativePath, File file) throws IOException {
        ChunkedFile chunkedFile = new ChunkedFile(file);
        chunkedFiles.add(chunkedFile);
        argsList.add(relativePath.toString());
        argsList.add(String.valueOf(chunkedFile.length()));
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
        for (int i = 1; i < args.length; i += 2) {
            String name = args[i];
            long length = Long.parseLong(args[i + 1]);
            addItem(name, length);
        }
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
        return lastSelectedDir;
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
        hideContextMenu();

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
        hideContextMenu();
        ObservableList<String> selectedItems = listView.getSelectionModel().getSelectedItems();

        MenuItem downloadSelected = new MenuItem("Download selected to...");
        MenuItem deleteSelected = new MenuItem("Delete selected");
        MenuItem createDir = new MenuItem("Create directory here");
        MenuItem rename = new MenuItem("Rename");

        downloadSelected.setOnAction(event -> onContextDownload(event, selectedItems));
        deleteSelected.setOnAction(event -> onContextDelete(event, selectedItems));
        createDir.setOnAction(this::onContextCreateDir);
        rename.setOnAction(event -> onContextRename(event, selectedItems));

        ContextMenu contextMenu = new ContextMenu(
                downloadSelected,
                deleteSelected,
                createDir,
                rename
        );
        contextMenu.setAutoHide(true);
        contextMenu.show(listView, contextMenuEvent.getScreenX(), contextMenuEvent.getScreenY());
        lastOpenedMenu = contextMenu;
    }

    private void onContextRename(ActionEvent actionEvent, ObservableList<String> selectedItems) {
        if (selectedItems.size() != 1) {
            System.err.println("wrong selection (must be 1 object)");
            return;
        }
        String oldName = selectedItems.get(0);
        TextInputDialog dialog = new TextInputDialog(oldName);
        dialog.setTitle("Rename");
        dialog.setHeaderText("Type new name...");
        runLater(() -> {
            dialog.showAndWait();
            dialog.setResultConverter(param -> param.getButtonData().isDefaultButton() ? param.getText() : null);
            if (dialog.getResult() != null && !dialog.getResult().isEmpty()) {
                String oldPath = currentPath.resolve(oldName).toString();
                String newPath = currentPath.resolve(dialog.getResult()).toString();
                Command rename = new Command(KnownCommands.Move, oldPath, newPath);
                Factory.getNetworkService().sendCommand(rename);
                localStructureRequest();
            }
        });
    }

    private void onContextCreateDir(ActionEvent event) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Directory creation");
        dialog.setHeaderText("Type name of the directory...");
        runLater(() -> {
            dialog.showAndWait();
            dialog.setResultConverter(param -> param.getButtonData().isDefaultButton() ? param.getText() : null);
            if (dialog.getResult() != null && !dialog.getResult().isEmpty()) {
                String dirName = currentPath.resolve(dialog.getResult()).toString();
                Command createDir = new Command(KnownCommands.CreateDirectory, dirName);
                Factory.getNetworkService().sendCommand(createDir);
                localStructureRequest();
            }
        });
    }

    private void onContextDownload(ActionEvent event, ObservableList<String> selectedItems) {
        NetworkService networkService = Factory.getNetworkService();
        List<String> requestedFiles = new ArrayList<>();

        DirectoryChooser directoryChooser = new DirectoryChooser();
        File file = directoryChooser.showDialog(App.getWindow());

        lastSelectedDir = file.toPath();

        for (String selectedItem : selectedItems) {
            Path relative = currentPath.resolve(selectedItem);
            requestedFiles.add(relative.toString());
        }

        setBlockedState(true, "Downloading...");
        Command req = new Command(KnownCommands.FileRequest, requestedFiles);
        networkService.sendCommand(req);
    }

    //TODO doesn't work for directories for now
    private void onContextDownloadOld(ActionEvent event, ObservableList<String> selectedItems) {
        if (selectedItems.isEmpty()) {
            System.err.println("Selection is empty");
            return;
        }
        FileTransferHelperService helperService = Factory.getFileTransferService();
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File file = directoryChooser.showDialog(App.getWindow());

        List<String> filesToReceive = new ArrayList<>();
        for (String selectedItem : selectedItems) {
            if (isDirectory(selectedItem)) {
                System.err.println("directories unsupported");
                return;
            }
            Path fileResultPath = file.toPath().resolve(selectedItem);
            long length = shownItems.get(selectedItem);
            helperService.queueFileReceive(fileResultPath, length);
            filesToReceive.add(currentPath.resolve(selectedItem).toString());
        }
        Command fileRequest = new Command(KnownCommands.FileRequest, filesToReceive);
        Factory.getPipelineManager().setup(PipelineSetup.FILE.handlers);
        Factory.getNetworkService().sendCommand(fileRequest);
        setBlockedState(true, "Downloading...");
        // File Inbound handler turns back to commands when finishes
    }

    private void onContextDelete(ActionEvent event, ObservableList<String> selectedItems) {
        if (selectedItems.isEmpty()) {
            System.err.println("Selection is empty");
            return;
        }
        List<String> pathsToDelete = new ArrayList<>();
        for (String selectedItem : selectedItems) {
            Path path = currentPath.resolve(selectedItem);
            pathsToDelete.add(path.toString());
        }
        Command deleteCommand = new Command(KnownCommands.Delete, pathsToDelete);
        Factory.getNetworkService().sendCommand(deleteCommand);
        localStructureRequest();
    }

    private void localStructureRequest() {
        Factory.getNetworkService().sendCommand(new Command(KnownCommands.LocalStructureRequest, currentPath.toString()));
    }

    private void hideContextMenu() {
        if (lastOpenedMenu != null) {
            lastOpenedMenu.hide();
        }
    }

    @FXML
    public void onRefreshButton(ActionEvent actionEvent) {
        localStructureRequest();
    }

    private static class DoubleClickChecker {
        private long trackTime;
        private boolean checkedOneTime;
        private String lastClickedItem;
        private final int delay;

        public DoubleClickChecker(int delayMillis) {
            this.delay = delayMillis;
            lastClickedItem = "";
        }

        /**
         * @return true if it was the second click on the same item
         */
        public boolean check(String item) {
            boolean result = false;
            if (!lastClickedItem.equals(item)) {
                checkedOneTime = false;
            }
            if (!checkedOneTime) {
                checkedOneTime = true;
            } else if (System.currentTimeMillis() - trackTime < delay) {
                checkedOneTime = false;
                result = true;
            }
            trackTime = System.currentTimeMillis();
            lastClickedItem = item;
            return result;
        }
    }
}
