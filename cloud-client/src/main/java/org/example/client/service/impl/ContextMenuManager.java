package org.example.client.service.impl;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.ContextMenuEvent;
import javafx.stage.DirectoryChooser;
import org.example.client.App;
import org.example.client.factory.Factory;
import org.example.client.service.ContextMenuService;
import org.example.client.service.NetworkService;
import org.example.domain.Command;
import org.example.domain.KnownCommands;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ContextMenuManager implements ContextMenuService {

    private ContextMenuManager() {}

    public static final ContextMenuService Instance = new ContextMenuManager();

    private ContextMenu lastOpenedMenu;
    private Path lastSelectedDir = Path.of("");
    private Path currentPath = Path.of("");

    @Override
    public void onContextMenu(ContextMenuEvent contextMenuEvent, Path currentPath, ListView<String> listView) {
        this.currentPath = currentPath;
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

    @Override
    public void onContextRename(ActionEvent actionEvent, ObservableList<String> selectedItems) {
        if (selectedItems.size() != 1) {
            System.err.println("wrong selection (must be 1 object)");
            return;
        }
        String oldName = selectedItems.get(0);
        TextInputDialog dialog = new TextInputDialog(oldName);
        dialog.setTitle("Rename");
        dialog.setHeaderText("Type new name...");
        Platform.runLater(() -> {
            dialog.showAndWait();
            dialog.setResultConverter(param -> param.getButtonData().isDefaultButton() ? param.getText() : null);
            if (dialog.getResult() != null && !dialog.getResult().isEmpty()) {
                String oldPath = currentPath.resolve(oldName).toString();
                String newPath = currentPath.resolve(dialog.getResult()).toString();
                Command rename = new Command(KnownCommands.Move, oldPath, newPath);
                Factory.getNetworkService().sendCommand(rename);
                Factory.getControllerService().localStructureRequest();
            }
        });
    }

    @Override
    public void onContextCreateDir(ActionEvent event) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Directory creation");
        dialog.setHeaderText("Type name of the directory...");
        Platform.runLater(() -> {
            dialog.showAndWait();
            dialog.setResultConverter(param -> param.getButtonData().isDefaultButton() ? param.getText() : null);
            if (dialog.getResult() != null && !dialog.getResult().isEmpty()) {
                String dirName = currentPath.resolve(dialog.getResult()).toString();
                Command createDir = new Command(KnownCommands.CreateDirectory, dirName);
                Factory.getNetworkService().sendCommand(createDir);
                Factory.getControllerService().localStructureRequest();
            }
        });
    }

    @Override
    public void onContextDownload(ActionEvent event, ObservableList<String> selectedItems) {
        NetworkService networkService = Factory.getNetworkService();
        List<String> requestedFiles = new ArrayList<>();

        DirectoryChooser directoryChooser = new DirectoryChooser();
        File file = directoryChooser.showDialog(App.getWindow());

        lastSelectedDir = file.toPath();

        for (String selectedItem : selectedItems) {
            Path relative = currentPath.resolve(selectedItem);
            requestedFiles.add(relative.toString());
        }

        Factory.getControllerService().setBlockedState(true, "Downloading...");
        Command req = new Command(KnownCommands.FileRequest, requestedFiles);
        networkService.sendCommand(req);
    }

    @Override
    public void onContextDelete(ActionEvent event, ObservableList<String> selectedItems) {
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
        Factory.getControllerService().localStructureRequest();
    }

    @Override
    public void hideContextMenu() {
        if (lastOpenedMenu != null) {
            lastOpenedMenu.hide();
        }
    }

    @Override
    public Path getLastSelectedDir() {
        return lastSelectedDir;
    }
}
