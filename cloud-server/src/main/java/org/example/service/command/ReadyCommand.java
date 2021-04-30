package org.example.service.command;

import io.netty.channel.ChannelHandlerContext;
import org.example.domain.Command;
import org.example.domain.KnownCommands;
import org.example.factory.Factory;
import org.example.service.CommandService;
import org.example.service.FileTransferHelperService;

public class ReadyCommand implements CommandService {
    @Override
    public void processCommand(ChannelHandlerContext ctx, Command command) {
        FileTransferHelperService helperService = Factory.getFileTransferService();
        helperService.triggerReady();
    }

    @Override
    public String getCommand() {
        return KnownCommands.Ready.name;
    }
}
