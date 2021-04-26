package org.example.service;

import io.netty.channel.ChannelPipeline;

/**
 * Manages the ChannelPipeline's state
 */
public interface PipelineManagerService {

    void setPipeline(ChannelPipeline pipeline);
    void setup(PipelineSetup setup);
}
