package org.example.service.impl;

import org.example.service.FileStorageService;

import java.nio.file.Path;

public class ClientFileStorage extends FileStorage {

    private ClientFileStorage() {}

    public static final FileStorageService Instance = new ClientFileStorage();

    @Override
    protected Path constructPath(Path relativePath) {
        return relativePath;
    }
}
