package org.example.client.service.command;

import io.netty.channel.ChannelHandlerContext;
import org.example.domain.Command;
import org.example.domain.KnownCommands;
import org.example.client.factory.Factory;
import org.example.service.CommandService;
import org.example.service.FileTransferHelperService;
import org.example.service.PipelineManagerService;
import org.example.client.service.PipelineSetup;

import java.nio.file.Path;

public class FileTransfer implements CommandService {
    @Override
    public void processCommand(ChannelHandlerContext ctx, Command command) {
        String[] args = command.getArgs();
        FileTransferHelperService helperService = Factory.getFileTransferService();
        Path selectedPath = Factory.getControllerService().lastSelectedDir();
        for (int i = 0; i < args.length; i += 2) {
            Path path = selectedPath.resolve(Path.of(args[i]));
            long length = Long.parseLong(args[i + 1]);
            helperService.queueFileReceive(path, length);
        }
        PipelineManagerService pipelineManager = Factory.getPipelineManager();
        pipelineManager.setup(PipelineSetup.FILE.handlers);
        ctx.writeAndFlush(new Command(KnownCommands.Ready));
    }

    @Override
    public KnownCommands getCommand() {
        return KnownCommands.FileTransfer;
    }
}
