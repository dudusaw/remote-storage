package org.example.service;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelPipeline;

/**
 * Manages the ChannelPipeline's state
 */
public interface PipelineManagerService {

    void setPipeline(ChannelPipeline pipeline);
    void setup(Class<? extends ChannelHandler>[] handlerClasses);
}
