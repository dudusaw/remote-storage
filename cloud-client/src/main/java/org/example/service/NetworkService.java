package org.example.service;

import io.netty.handler.stream.ChunkedFile;
import org.example.domain.Command;

public interface NetworkService {

    void sendFile(ChunkedFile file);

    void sendCommand(Command cmd);

    void closeConnectionIfExists();

    void connect(String host, int port);

    void setupPipeline(PipelineSetup setup);

}
