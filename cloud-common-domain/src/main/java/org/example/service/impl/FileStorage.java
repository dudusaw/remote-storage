package org.example.service.impl;

import io.netty.buffer.ByteBuf;
import io.netty.handler.stream.ChunkedFile;
import org.example.service.FileStorageService;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public abstract class FileStorage implements FileStorageService {

    private OutputStream outputStream;

    @Override
    public void beginWriteFile(Path relativePath) {
        if (isActive()) {
            throw new IllegalStateException("Previous file wasn't closed properly");
        }
        try {
            Path file = constructPath(relativePath);
            Files.deleteIfExists(file);
            Files.createDirectories(file.getParent());
            outputStream = new BufferedOutputStream(Files.newOutputStream(file, StandardOpenOption.CREATE_NEW));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void writeToActiveFile(ByteBuf data, int bytesCount) {
        if (!isActive()) {
            throw new IllegalStateException("Isn't currently ready to write");
        }
        try {
            data.readBytes(outputStream, bytesCount);
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void endWriteFile() {
        try {
            if (isActive()) {
                outputStream.close();
                outputStream = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isActive() {
        return outputStream != null;
    }

    @Override
    public void delete(Path relativePath) {
        Path path = constructPath(relativePath);
        try {
            if (Files.isDirectory(path)) {
                Files.walkFileTree(path, new SimpleFileVisitor<>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        Files.delete(file);
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                        Files.delete(file);
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                        if (exc != null) {
                            throw exc;
                        }
                        Files.delete(dir);
                        return FileVisitResult.CONTINUE;
                    }
                });
            } else {
                Files.deleteIfExists(path);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public ChunkedFile readFile(Path relativePath) {
        try {
            Path file = constructPath(relativePath);
            return new ChunkedFile(new File(file.toString()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Path getStoragePath() {
        throw new UnsupportedOperationException();
    }

    protected abstract Path constructPath(Path relativePath);
}
