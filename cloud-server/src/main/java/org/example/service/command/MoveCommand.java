package org.example.service.command;

import io.netty.channel.ChannelHandlerContext;
import org.example.domain.Command;
import org.example.domain.KnownCommands;
import org.example.factory.Factory;
import org.example.service.CommandService;
import org.example.service.FileStorageService;

import java.io.IOException;
import java.nio.file.*;

public class MoveCommand implements CommandService {
    @Override
    public void processCommand(ChannelHandlerContext ctx, Command command) {
        verifyArgs(command);
        try {
            FileStorageService storageService = Factory.getStorageService();
            Path from = storageService.getStoragePath().resolve(Paths.get(command.getArgs()[0]));
            Path to = storageService.getStoragePath().resolve(Paths.get(command.getArgs()[1]));
            Files.move(from, to);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void verifyArgs(Command command) {
        String[] args = command.getArgs();
        if (args.length != 2) {
            throw new IllegalArgumentException("wrong args");
        }
    }

    @Override
    public String getCommand() {
        return KnownCommands.Move.name;
    }
}
