package org.example.service.command;

import io.netty.channel.ChannelHandlerContext;
import org.example.domain.Command;
import org.example.domain.KnownCommands;
import org.example.service.CommandService;
import org.example.service.FileStorageService;
import org.example.factory.Factory;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * Result structure is only 1 level from the directory.
 * So only files and directories inside the provided directory are returned.
 * Only 1 arg is the dir to look from.
 * If no args were provided, it assumes the root dir as argument.
 * Returns the active directory's path or empty string if it's a root directory as first arg .
 * File's/dir's name as second arg.
 * Length in bytes as third arg. (-1 for dirs)
 * 2 and 3 args repeats for every entity.
 */
public class LocalStructureRequestCommand implements CommandService {
    @Override
    public void processCommand(ChannelHandlerContext ctx, Command command) {
        try {
            List<String> paths = new ArrayList<>();
            FileStorageService storageService = Factory.getStorageService();
            Path startPath = storageService.getStoragePath();
            if (command.getArgs().length > 0) {
                String requestedPath = command.getArgs()[0];
                startPath = startPath.resolve(Paths.get(requestedPath));
                paths.add(requestedPath);
            } else {
                paths.add("");
            }
            System.out.println(startPath);
            if (!Files.isDirectory(startPath)) {
                throw new IllegalArgumentException("received path isn't a directory or doesn't exist");
            }
            Path finalStartPath = startPath;
            Files.walkFileTree(startPath, EnumSet.noneOf(FileVisitOption.class), 1, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Path resultPath = file.subpath(finalStartPath.getNameCount(), file.getNameCount());
                    paths.add(resultPath.toString());
                    long size = attrs.isDirectory() ? -1 : attrs.size();
                    paths.add(String.valueOf(size));
                    return FileVisitResult.CONTINUE;
                }
            });

            String[] args = new String[paths.size()];
            paths.toArray(args);
            Command response = new Command(KnownCommands.LocalStructureRequest, args);
            ctx.writeAndFlush(response);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getCommand() {
        return KnownCommands.LocalStructureRequest.name;
    }
}
