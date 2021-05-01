package org.example.service.command;

import io.netty.channel.ChannelHandlerContext;
import org.example.domain.Command;
import org.example.domain.KnownCommands;
import org.example.factory.Factory;
import org.example.service.CommandService;

public class LocalStructureRequest implements CommandService {
    @Override
    public void processCommand(ChannelHandlerContext ctx, Command command) {
        Factory.getControllerService().updateViewLocal(command.getArgs());
    }

    @Override
    public String getCommand() {
        return KnownCommands.LocalStructureRequest.name;
    }
}
