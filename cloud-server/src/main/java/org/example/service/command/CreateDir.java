package org.example.service.command;

import io.netty.channel.ChannelHandlerContext;
import org.example.domain.Command;
import org.example.domain.KnownCommands;
import org.example.factory.Factory;
import org.example.service.CommandService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Creates a directory with a specified path
 */
public class CreateDir implements CommandService {
    @Override
    public void processCommand(ChannelHandlerContext ctx, Command command) {
        Path root = Factory.getStorageService().getStoragePath().resolve(command.getArgs()[0]);
        try {
            Files.createDirectories(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public KnownCommands getCommand() {
        return KnownCommands.CreateDirectory;
    }
}
