package org.example.service.command;

import io.netty.channel.ChannelHandlerContext;
import org.example.domain.Command;
import org.example.domain.KnownCommands;
import org.example.factory.Factory;
import org.example.service.CommandService;
import org.example.service.FileTransferHelperService;

public class Ready implements CommandService {
    @Override
    public void processCommand(ChannelHandlerContext ctx, Command command) {
        FileTransferHelperService helperService = Factory.getFileTransferService();
        helperService.triggerNextCommandType(KnownCommands.Ready, command);
    }

    @Override
    public KnownCommands getCommand() {
        return KnownCommands.Ready;
    }
}
