package org.example.service;

import io.netty.buffer.ByteBuf;
import org.example.domain.Command;
import org.example.domain.KnownCommands;

import java.nio.file.Path;
import java.util.function.Consumer;

public interface FileTransferHelperService {

    /**
     * Prepares the file for reading with specified length in bytes
     */
    void queueFileReceive(Path path, long bytes);

    /**
     * Adds an action to the queue, executes when the next command of the provided type received
     */
    void queueCommandCallback(KnownCommands commandType, Consumer<Command> action);

    void discardNextCommandType(KnownCommands commandType);

    /**
     * Executes next action in queue if there are any for this command type
     */
    void triggerNextCommandType(KnownCommands type, Command command);

    /**
     * Reads the provided bytes and stores them in corresponding file
     * @return true if all pending files have been read, false otherwise
     */
    boolean readBytes(ByteBuf data);
}
