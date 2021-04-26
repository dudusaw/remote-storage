package org.example.service.impl;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import org.example.service.PipelineManagerService;
import org.example.service.PipelineSetup;

import java.lang.reflect.InvocationTargetException;

public class PipelineManager implements PipelineManagerService {

    private PipelineManager() {}

    public static final PipelineManagerService Instance = new PipelineManager();

    private ChannelPipeline pipeline;

    @Override
    public void setPipeline(ChannelPipeline pipeline) {
        this.pipeline = pipeline;
    }

    @Override
    public void setup(PipelineSetup setup) {
        if (pipeline == null) {
            throw new IllegalStateException("pipeline wasn't set");
        }

        // Add new handlers to the pipeline first, then remove old ones
        // That way it should work properly if we want to write through the pipeline immediately after setup
        int clearCount = getPipelineLength();
        constructAndAddHandlers(setup);
        clearPipeLast(clearCount);
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

    private void constructAndAddHandlers(PipelineSetup setup) {
        ChannelHandler[] handlers = new ChannelHandler[setup.handlerClasses.length];
        for (int i = 0; i < setup.handlerClasses.length; i++) {
            try {
                handlers[i] = setup.handlerClasses[i].getDeclaredConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
        pipeline.addFirst(handlers);
    }
}
