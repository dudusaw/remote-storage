package org.example.service.impl;

import io.netty.buffer.ByteBuf;
import org.example.service.FileStorageService;
import org.example.service.FileTransferHelperService;
import org.example.domain.VoidFunction;

import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Queue;

public class FileTransferHelper implements FileTransferHelperService {

    private FileTransferHelper() { }

    public static final FileTransferHelper Instance = new FileTransferHelper();

    private static class FileInfo {
        Path path;
        long bytes;

        public FileInfo(Path path, long bytes) {
            this.path = path;
            this.bytes = bytes;
        }
    }

    private final Queue<FileInfo> fileQueue = new ArrayDeque<>();
    private final Queue<VoidFunction> waitingForReady = new ArrayDeque<>();
    private FileStorageService fileStorageService;

    public void setFileStorageService(FileStorageService fss) {
        fileStorageService = fss;
    }

    @Override
    public void queueFileReceive(Path path, long bytes) {
        fileQueue.add(new FileInfo(path, bytes));
    }

    @Override
    public void queueReadyCallback(VoidFunction action) {
        waitingForReady.add(action);
    }

    @Override
    public void triggerReady() {
        if (waitingForReady.isEmpty()) return;
        waitingForReady.remove().act();
    }

    @Override
    public boolean readBytes(ByteBuf data) {
        FileInfo currentFile = fileQueue.element();

        if (!fileStorageService.isActive()) {
            fileStorageService.beginWriteFile(currentFile.path);
        }

        if (data.readableBytes() > currentFile.bytes) {
            fileStorageService.writeToActiveFile(data, (int) currentFile.bytes);
            fileStorageService.endWriteFile();
            fileQueue.poll();

            currentFile = fileQueue.element();
            int bytesToNextFile = data.readableBytes();
            fileStorageService.beginWriteFile(currentFile.path);
            fileStorageService.writeToActiveFile(data, bytesToNextFile);
            currentFile.bytes -= bytesToNextFile;
        } else {
            int bytesToNextFile = data.readableBytes();
            fileStorageService.writeToActiveFile(data, bytesToNextFile);
            currentFile.bytes -= bytesToNextFile;
        }

        if (currentFile.bytes == 0) {
            fileQueue.poll();
            fileStorageService.endWriteFile();
            return fileQueue.isEmpty();
        }
        return false;
    }
}
