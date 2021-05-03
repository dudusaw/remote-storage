package org.example.client.service;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.ListView;
import javafx.scene.input.ContextMenuEvent;

import java.nio.file.Path;

public interface ContextMenuService {
    void onContextMenu(ContextMenuEvent contextMenuEvent, Path currentPath, ListView<String> listView);

    void onContextRename(ActionEvent actionEvent, ObservableList<String> selectedItems);

    void onContextCreateDir(ActionEvent event);

    void onContextDownload(ActionEvent event, ObservableList<String> selectedItems);

    void onContextDelete(ActionEvent event, ObservableList<String> selectedItems);

    void hideContextMenu();

    Path getLastSelectedDir();
}
