package org.example.service;

import io.netty.buffer.ByteBuf;
import io.netty.handler.stream.ChunkedFile;

import java.nio.file.Path;

public interface FileStorageService {

    void beginWriteFile(Path relativePath);

    void writeToActiveFile(ByteBuf data, int bytesCount);

    void endWriteFile();

    boolean isActive();

    void delete(Path relativePath);

    ChunkedFile readFile(Path relativePath);

    Path getStoragePath();
}
