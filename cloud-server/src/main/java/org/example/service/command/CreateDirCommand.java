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
public class CreateDirCommand implements CommandService {
    @Override
    public void processCommand(ChannelHandlerContext ctx, Command command) {
        Path root = Factory.getStorageService().getStoragePath().resolve(command.getArgs()[0]);
        try {
            Files.createDirectory(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getCommand() {
        return KnownCommands.CreateDirectory.name;
    }
}
