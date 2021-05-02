package org.example.service;

import io.netty.channel.ChannelHandlerContext;
import org.example.domain.Command;
import org.example.domain.KnownCommands;

public interface CommandService {

    void processCommand(ChannelHandlerContext ctx, Command command);

    KnownCommands getCommand();

}