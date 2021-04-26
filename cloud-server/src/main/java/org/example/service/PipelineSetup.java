package org.example.service;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.stream.ChunkedWriteHandler;
import org.example.service.handler.CommandInboundHandler;
import org.example.service.handler.FileInboundHandler;

import java.lang.reflect.InvocationTargetException;

/**
 * Manages the ChannelPipeline's state
 */
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

    private final Class<? extends ChannelHandler>[] handlerClasses;

    private static ChannelPipeline pipeline;
    private static PipelineSetup currentSetup;

    PipelineSetup(Class<? extends ChannelHandler>... handlerClasses) {
        this.handlerClasses = handlerClasses;
    }

    public static void setPipeline(ChannelPipeline pipeline) {
        PipelineSetup.pipeline = pipeline;
    }

    public static PipelineSetup getCurrentSetup() {
        return currentSetup;
    }

    public void setup() {
        if (pipeline == null) {
            throw new IllegalStateException("pipeline wasn't set");
        }

        // Add new handlers to the pipeline first, then remove old ones
        // That way it should work properly if we want to write through the pipeline immediately after setup
        int clearCount = getPipelineLength();
        constructAndAddHandlers();
        clearPipeLast(clearCount);
        currentSetup = this;
    }

    private void clearPipeLast(int clearCount) {
        for (int i = 0; i < clearCount; i++) {
            pipeline.removeLast();
        }
    }

    private int getPipelineLength() {
        int c = 0;
        for (var ignored : pipeline) {
            c++;
        }
        return c;
    }

    private void constructAndAddHandlers() {
        ChannelHandler[] handlers = new ChannelHandler[handlerClasses.length];
        for (int i = 0; i < handlerClasses.length; i++) {
            try {
                handlers[i] = handlerClasses[i].getDeclaredConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
        pipeline.addFirst(handlers);
    }

    public static class MyObjectDecoder extends ObjectDecoder {

        public MyObjectDecoder() {
            super(ClassResolvers.weakCachingResolver(null));
        }
    }
}
