package org.example.service;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.stream.ChunkedWriteHandler;
import org.example.domain.service.impl.MyObjectDecoder;
import org.example.service.handler.CommandInboundHandler;
import org.example.service.handler.FileInboundHandler;

import java.lang.reflect.InvocationTargetException;

public enum PipelineSetup {

    // ObjectDecoder isn't sharable, so we have to instantiate a new one every time we do pipeline replacement
    COMMAND(
            ObjectEncoder.class,
            MyObjectDecoder.class,
            CommandInboundHandler.class
    ),
    FILE(
            ChunkedWriteHandler.class,
            FileInboundHandler.class
    ),
    ;

    public final Class<? extends ChannelHandler>[] handlerClasses;

    @SafeVarargs
    PipelineSetup(Class<? extends ChannelHandler>... handlerClasses) {
        this.handlerClasses = handlerClasses;
    }

}
