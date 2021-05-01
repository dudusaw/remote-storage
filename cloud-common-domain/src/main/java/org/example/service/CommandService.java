package org.example.service;

import io.netty.channel.ChannelHandlerContext;
import org.example.domain.Command;

public interface CommandService {

    void processCommand(ChannelHandlerContext ctx, Command command);

    String getCommand();

}