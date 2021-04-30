package org.example.service.command;

import io.netty.channel.ChannelHandlerContext;
import org.example.domain.Command;
import org.example.domain.KnownCommands;
import org.example.factory.Factory;
import org.example.service.CommandService;
import org.example.service.FileTransferHelperService;
import org.example.service.PipelineSetup;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Used for transferring files from client to server.
 * <p>
 * First argument is complete file's path relative to the server.
 * Second is file's exact length in bytes.
 * Arguments could repeat if we want to send multiple files at once, so args count must be even.
 * e.g. file with length 2GB stored on sender side.
 * Server's storage path: 'A:\storage\'.
 * We request to put it in '\some_package\abc1\my_file.txt' relative to the server's storage.
 * Then the result command: '/transf \some_package\abc1\my_file.txt 2147483648'.
 * Server should put it in 'A:\storage\some_package\abc1\my_file.txt'.
 */
public class FileTransferCommand implements CommandService {
    @Override
    public void processCommand(ChannelHandlerContext ctx, Command command) {
        verifyArgs(ctx, command);
        Factory.getPipelineManager().setup(PipelineSetup.FILE.handlers);
        prepareFileRead(command);
        readyResponse(ctx);
    }

    private void verifyArgs(ChannelHandlerContext ctx, Command command) {
        String[] args = command.getArgs();
        if (args == null || args.length == 0 || args.length % 2 != 0) {
            ctx.writeAndFlush(new Command(KnownCommands.Fail));
            throw new IllegalArgumentException("wrong command received");
        }
    }

    private void prepareFileRead(Command command) {
        FileTransferHelperService transferHelper = Factory.getFileTransferService();
        for (int i = 0; i < command.getArgs().length; i += 2) {
            Path path = Paths.get(command.getArgs()[i]);
            long length = Long.parseLong(command.getArgs()[i + 1]);
            transferHelper.queueFileReceive(path, length);
        }
    }

    private void readyResponse(ChannelHandlerContext ctx) {
        ctx.writeAndFlush(new Command(KnownCommands.Ready));
    }

    @Override
    public String getCommand() {
        return KnownCommands.FileTransfer.name;
    }
}
