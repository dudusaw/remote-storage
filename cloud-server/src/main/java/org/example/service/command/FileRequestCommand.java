package org.example.service.command;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.stream.ChunkedFile;
import org.example.domain.Command;
import org.example.domain.KnownCommands;
import org.example.factory.Factory;
import org.example.service.CommandService;
import org.example.domain.service.FileStorageService;
import org.example.service.PipelineSetup;
import org.example.domain.service.VoidFunction;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileRequestCommand implements CommandService {
    @Override
    public void processCommand(ChannelHandlerContext ctx, Command command) {
        verifyArgs(command);

        /*
             1
             this command received
             send back FileTransferCommand with all pending files and lengths
             turn into File mode
             wait for ready command
             send files
             turn into Command mode
             send ready command


             2
             client side:
             prepare for receive files (we should know lengths of the files we want to get, this is done by FileStructureRequestCommand)
             construct and send this command with all files we want to retrieve from the storage
             turn into file mode
             write down the received bytes
             turn command mode
             wait for ready command
             ...now client is ready to send next commands


             server side:
             this command received
             turn into File mode
             send files
             turn into Command mode
             send ready Command
        */
        PipelineSetup.FILE.setup();
        FileStorageService storageService = Factory.getStorageService();
        Counter counter = new Counter(command.getArgs().length, () -> responseAfterAllWritingDone(ctx));
        for (String arg : command.getArgs()) {
            ChunkedFile file = storageService.readFile(Paths.get(arg));
            ctx.writeAndFlush(file).addListener(future -> counter.count());
        }
    }

    private void responseAfterAllWritingDone(ChannelHandlerContext ctx) {
        PipelineSetup.COMMAND.setup();
        //Command readyResponse = new Command(KnownCommands.Ready);
        //ctx.writeAndFlush(readyResponse);
    }


    /**
     * 'Action' event executes when count() called 'amount' times
     */
    private static class Counter {
        private int amount;
        private final VoidFunction action;

        public Counter(int amount, VoidFunction action) {
            this.amount = amount;
            this.action = action;
        }

        public void count() {
            if (amount <= 0) {
                throw new IllegalStateException("count exceeded amount");
            }
            amount--;
            if (amount == 0) {
                action.act();
            }
        }
    }

    private void verifyArgs(Command command) {
        String[] args = command.getArgs();
        if (args == null || args.length == 0) {
            throw new IllegalArgumentException("wrong args");
        }
    }

    @Override
    public String getCommand() {
        return KnownCommands.FileRequest.name;
    }
}
