package org.example.service.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.example.domain.Command;
import org.example.domain.KnownCommands;
import org.example.factory.Factory;

public class ClientCommandInboundHandler extends SimpleChannelInboundHandler<Command> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Command msg) throws Exception {
        if (msg.getCommandName().equals(KnownCommands.LocalStructureRequest.name)) {
            Factory.getControllerService().updateViewLocal(msg.getArgs());
        } else if (msg.getCommandName().equals(KnownCommands.Ready.name)) {
            Factory.getFileTransferService().triggerReady();
        }
    }
}
