package org.example.client.service;

import java.nio.file.Path;

public interface ControllerService {

    void updateViewLocal(String[] args);
    void setBlockedState(boolean blocked, String statusMessage);
    Path lastSelectedDir();

    void localStructureRequest();
}
