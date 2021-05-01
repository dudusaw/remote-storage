package org.example.service.command;

import io.netty.channel.ChannelHandlerContext;
import org.example.domain.Command;
import org.example.domain.KnownCommands;
import org.example.factory.Factory;
import org.example.service.CommandService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class CreateDir implements CommandService {
    @Override
    public void processCommand(ChannelHandlerContext ctx, Command command) {
        try {
            Path dir = Path.of(command.getArgs()[0]);
            Path lastSelected = Factory.getControllerService().lastSelectedDir();
            Files.createDirectories(lastSelected.resolve(dir));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getCommand() {
        return KnownCommands.CreateDirectory.name;
    }
}
