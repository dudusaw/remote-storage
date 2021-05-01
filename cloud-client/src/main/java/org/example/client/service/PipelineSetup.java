package org.example.client.service;

import io.netty.channel.ChannelHandler;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.stream.ChunkedWriteHandler;
import org.example.service.impl.MyObjectDecoder;
import org.example.client.service.handler.ClientCommandInboundHandler;
import org.example.client.service.handler.ClientFileInboundHandler;

public enum PipelineSetup {

    // ObjectDecoder isn't sharable, so we have to instantiate a new one every time we do pipeline replacement
    COMMAND(
            ObjectEncoder.class,
            MyObjectDecoder.class,
            ClientCommandInboundHandler.class
    ),
    FILE(
            ObjectEncoder.class,
            ChunkedWriteHandler.class,
            ClientFileInboundHandler.class
    ),
    ;

    public final Class<? extends ChannelHandler>[] handlers;

    @SafeVarargs
    PipelineSetup(Class<? extends ChannelHandler>... handlers) {
        this.handlers = handlers;
    }

}
