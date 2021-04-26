package org.example.service;

public interface NetworkService {

    void sendCommand(String command);

    int readCommandResult(byte[] buffer);

    void closeConnectionIfExists();

    void connect(String host, int port);

}
