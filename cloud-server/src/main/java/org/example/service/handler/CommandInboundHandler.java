package org.example.service.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.example.domain.Command;
import org.example.factory.Factory;

public class CommandInboundHandler extends SimpleChannelInboundHandler<Command> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Command command) throws Exception {
        Factory.getCommandProcessor().processCommand(ctx, command);
    }
}
