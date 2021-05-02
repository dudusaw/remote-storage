package org.example.client.service.command;

import io.netty.channel.ChannelHandlerContext;
import org.example.client.factory.Factory;
import org.example.domain.Command;
import org.example.domain.KnownCommands;
import org.example.service.CommandService;

public class Fail implements CommandService {
    @Override
    public void processCommand(ChannelHandlerContext ctx, Command command) {
        Factory.getFileTransferService().triggerNextCommandType(KnownCommands.Fail, command);
    }

    @Override
    public KnownCommands getCommand() {
        return KnownCommands.Fail;
    }
}
