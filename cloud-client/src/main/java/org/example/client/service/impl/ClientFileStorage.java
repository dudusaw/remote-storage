package org.example.client.service.impl;

import org.example.service.FileStorageService;
import org.example.service.impl.FileStorage;

import java.nio.file.Path;

public class ClientFileStorage extends FileStorage {

    private ClientFileStorage() {}

    public static final FileStorageService Instance = new ClientFileStorage();

    @Override
    protected Path constructPath(Path relativePath) {
        return relativePath;
    }
}
