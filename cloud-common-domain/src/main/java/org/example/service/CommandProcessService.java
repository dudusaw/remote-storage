package org.example.service;

import io.netty.channel.ChannelHandlerContext;
import org.example.domain.Command;

public interface CommandProcessService {

    void processCommand(ChannelHandlerContext ctx, Command command);

}