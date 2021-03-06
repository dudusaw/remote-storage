package org.example.client.service.command;

import io.netty.channel.ChannelHandlerContext;
import org.example.domain.Command;
import org.example.domain.KnownCommands;
import org.example.client.factory.Factory;
import org.example.service.CommandService;

public class Ready implements CommandService {
    @Override
    public void processCommand(ChannelHandlerContext ctx, Command command) {
        Factory.getFileTransferService().triggerNextCommandType(KnownCommands.Ready, command);
    }

    @Override
    public KnownCommands getCommand() {
        return KnownCommands.Ready;
    }
}
