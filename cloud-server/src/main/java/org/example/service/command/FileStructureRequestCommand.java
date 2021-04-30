package org.example.service.command;

import io.netty.channel.ChannelHandlerContext;
import org.example.domain.Command;
import org.example.domain.KnownCommands;
import org.example.factory.Factory;
import org.example.service.CommandService;
import org.example.service.FileStorageService;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

/**
 * Sends all the directories/files that are currently stored on root.
 * First arg is file's(dir) path relative to the root.
 * Second arg is length in bytes (long). Returns -1 as second arg for directories.
 * Then, first two args repeats for all entities.
 */
public class FileStructureRequestCommand implements CommandService {
    @Override
    public void processCommand(ChannelHandlerContext ctx, Command command) {
        try {
            List<String> walkedFiles = new ArrayList<>();
            FileStorageService storageService = Factory.getStorageService();
            Path root = storageService.getStoragePath();
            Files.walkFileTree(root, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Path resultPath = file.subpath(root.getNameCount(), file.getNameCount());
                    walkedFiles.add(resultPath.toString());
                    walkedFiles.add(String.valueOf(attrs.size()));
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    if (!dir.equals(root)) {
                        if (exc != null) {
                            throw exc;
                        }
                        Path resultPath = dir.subpath(root.getNameCount(), dir.getNameCount());
                        walkedFiles.add(resultPath.toString());
                        walkedFiles.add(String.valueOf(-1));
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
            String[] args = new String[walkedFiles.size()];
            walkedFiles.toArray(args);
            Command response = new Command(KnownCommands.FileStructureRequest, args);
            ctx.writeAndFlush(response);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getCommand() {
        return KnownCommands.FileStructureRequest.name;
    }
}
