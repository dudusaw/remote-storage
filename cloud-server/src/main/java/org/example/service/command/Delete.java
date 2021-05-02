package org.example.service.command;

import io.netty.channel.ChannelHandlerContext;
import org.example.domain.Command;
import org.example.domain.KnownCommands;
import org.example.factory.Factory;
import org.example.service.CommandService;
import org.example.service.FileStorageService;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Delete implements CommandService {
    @Override
    public void processCommand(ChannelHandlerContext ctx, Command command) {
        String[] args = command.getArgs();
        FileStorageService fileStorageService = Factory.getStorageService();
        for (String arg : args) {
            Path path = Paths.get(arg);
            fileStorageService.delete(path);
        }
    }

    @Override
    public KnownCommands getCommand() {
        return KnownCommands.Delete;
    }
}
