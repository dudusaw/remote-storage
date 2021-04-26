package org.example.service.impl;

import org.example.domain.service.FileStorageService;
import org.example.domain.service.impl.FileStorage;

import java.nio.file.Path;

public class ClientFileStorage extends FileStorage {

    private ClientFileStorage() {}

    public static final FileStorageService Instance = new ClientFileStorage();

    @Override
    protected Path constructPath(Path relativePath) {
        return relativePath;
    }
}
