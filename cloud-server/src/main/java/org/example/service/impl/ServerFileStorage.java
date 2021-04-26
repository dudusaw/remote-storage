package org.example.service.impl;

import org.example.domain.service.impl.FileStorage;
import org.example.factory.Factory;

import java.nio.file.Path;
import java.nio.file.Paths;

public class ServerFileStorage extends FileStorage {

    private final Path StoragePath;

    private ServerFileStorage() {
        StoragePath = Paths.get(Factory.getConfigProperties().getProperty("AbsoluteStoragePath"));
    }

    public static final ServerFileStorage Instance = new ServerFileStorage();

    @Override
    public Path getStoragePath() {
        return StoragePath;
    }

    @Override
    protected Path constructPath(Path relativePath) {
        return StoragePath.resolve(relativePath);
    }
}
