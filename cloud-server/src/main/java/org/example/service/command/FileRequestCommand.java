package org.example.service.command;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.stream.ChunkedFile;
import org.example.domain.Command;
import org.example.domain.CommonUtil;
import org.example.domain.Counter;
import org.example.domain.KnownCommands;
import org.example.factory.Factory;
import org.example.service.CommandService;
import org.example.service.FileStorageService;
import org.example.service.FileTransferHelperService;
import org.example.service.PipelineSetup;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

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
        sendBy1Method(ctx, command);
        //sendBy2Method(ctx, command);
    }

    private void sendBy1Method(ChannelHandlerContext ctx, Command command) {
        try {
            List<String> filesToSend = new ArrayList<>();
            List<ChunkedFile> chunkedFiles = new ArrayList<>();
            FileStorageService storageService = Factory.getStorageService();
            FileTransferHelperService transferHelperService = Factory.getFileTransferService();

            for (String arg : command.getArgs()) {
                Path item = storageService.getStoragePath().resolve(arg);
                if (Files.isDirectory(item)) {
                    Files.walkFileTree(item, new SimpleFileVisitor<>() {
                        @Override
                        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                            if (!dir.equals(item) && CommonUtil.isDirectoryEmpty(dir)) {
                                Path relativeDirPath = dir.subpath(storageService.getStoragePath().getNameCount(), dir.getNameCount());
                                Command createDir = new Command(KnownCommands.CreateDirectory, relativeDirPath.toString());
                                ctx.writeAndFlush(createDir);
                            }
                            return FileVisitResult.CONTINUE;
                        }

                        @Override
                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                            Path relativeFilePath = file.subpath(storageService.getStoragePath().getNameCount(), file.getNameCount());
                            addFileToLists(filesToSend, chunkedFiles, relativeFilePath, file);
                            return FileVisitResult.CONTINUE;
                        }
                    });
                } else {
                    addFileToLists(filesToSend, chunkedFiles, Paths.get(arg), item);
                }
            }
            Command transf = new Command(KnownCommands.FileTransfer, filesToSend);
            ctx.writeAndFlush(transf);

            transferHelperService.queueReadyCallback(() -> {
                Counter counter = new Counter(chunkedFiles.size(), () -> responseAfterAllWritingDone(ctx));
                Factory.getPipelineManager().setup(PipelineSetup.FILE.handlers);
                for (ChunkedFile chunkedFile : chunkedFiles) {
                    ctx.writeAndFlush(chunkedFile).addListener(future -> counter.count());
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addFileToLists(List<String> filesToSend, List<ChunkedFile> chunkedFiles, Path relativePath, Path absolutePath) throws IOException {
        ChunkedFile chunkedFile = new ChunkedFile(new File(absolutePath.toString()));
        long length = chunkedFile.length();
        filesToSend.add(relativePath.toString());
        filesToSend.add(String.valueOf(length));
        chunkedFiles.add(chunkedFile);
    }

    private void sendBy2Method(ChannelHandlerContext ctx, Command command) {
        Factory.getPipelineManager().setup(PipelineSetup.FILE.handlers);
        FileStorageService storageService = Factory.getStorageService();
        Counter counter = new Counter(command.getArgs().length, () -> responseAfterAllWritingDone(ctx));
        for (String arg : command.getArgs()) {
            ChunkedFile file = storageService.readFile(Paths.get(arg));
            ctx.writeAndFlush(file).addListener(future -> counter.count());
        }
    }

    private void responseAfterAllWritingDone(ChannelHandlerContext ctx) {
        Factory.getPipelineManager().setup(PipelineSetup.COMMAND.handlers);
        ctx.writeAndFlush(new Command(KnownCommands.Ready));
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
