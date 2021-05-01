package org.example.service;

import io.netty.buffer.ByteBuf;
import org.example.domain.VoidFunction;

import java.nio.file.Path;

public interface FileTransferHelperService {

    /**
     * Prepares the file for reading with specified length in bytes
     */
    void queueFileReceive(Path path, long bytes);

    /**
     * Adds an action to the queue, executes when the next ready command received
     */
    void queueReadyCallback(VoidFunction action);

    /**
     * Executes next action in queue if there are any
     */
    void triggerReady();

    /**
     * Reads the provided bytes and stores them in corresponding file
     * @return true if all pending files have been read, false otherwise
     */
    boolean readBytes(ByteBuf data);
}
