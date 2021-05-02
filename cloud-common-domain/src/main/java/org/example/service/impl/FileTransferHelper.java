package org.example.service.impl;

import io.netty.buffer.ByteBuf;
import org.example.domain.Command;
import org.example.domain.KnownCommands;
import org.example.service.FileStorageService;
import org.example.service.FileTransferHelperService;

import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.EnumMap;
import java.util.Map;
import java.util.Queue;
import java.util.function.Consumer;

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
    private final Map<KnownCommands, Queue<Consumer<Command>>> waitingForCommand = new EnumMap<>(KnownCommands.class);
    private FileStorageService fileStorageService;

    public void setFileStorageService(FileStorageService fss) {
        fileStorageService = fss;
    }

    @Override
    public void queueFileReceive(Path path, long bytes) {
        fileQueue.add(new FileInfo(path, bytes));
    }

    @Override
    public void queueCommandCallback(KnownCommands commandType, Consumer<Command> action) {
        if (waitingForCommand.get(commandType) == null) {
            waitingForCommand.put(commandType, new ArrayDeque<>());
        }
        waitingForCommand
                .get(commandType)
                .add(action);
    }

    @Override
    public void discardNextCommandType(KnownCommands commandType) {
        waitingForCommand.get(commandType).poll();
    }

    @Override
    public void triggerNextCommandType(KnownCommands type, Command command) {
        if (waitingForCommand.get(type).isEmpty()) return;
        waitingForCommand
                .get(type)
                .remove()
                .accept(command);
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
