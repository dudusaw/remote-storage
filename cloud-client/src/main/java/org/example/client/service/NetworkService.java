package org.example.client.service;

import io.netty.handler.stream.ChunkedFile;
import org.example.domain.Command;

import java.util.function.Consumer;

public interface NetworkService {

    void sendFile(ChunkedFile file);

    void sendCommand(Command cmd);

    void closeConnectionIfExists();

    void connectWithCallback(String host, int port, Consumer<Boolean> onResult);

    void setupPipeline(PipelineSetup setup);

}
