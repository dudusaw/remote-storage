package org.example.client.service.impl;

import io.netty.handler.stream.ChunkedFile;
import org.example.client.factory.Factory;
import org.example.client.service.ClientFileManagerService;
import org.example.client.service.ControllerService;
import org.example.client.service.NetworkService;
import org.example.client.service.PipelineSetup;
import org.example.domain.Command;
import org.example.domain.CommonUtil;
import org.example.domain.KnownCommands;
import org.example.service.FileTransferHelperService;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

public class ClientFileManager implements ClientFileManagerService {

    private ClientFileManager() {}

    public static final ClientFileManagerService Instance = new ClientFileManager();

    @Override
    public void sendFilesToActivePath(List<File> files, Path currentPath) {
        try {
            ControllerService controllerService = Factory.getControllerService();
            FileTransferHelperService transferHelperService = Factory.getFileTransferService();
            NetworkService networkService = Factory.getNetworkService();

            List<String> argsList = new ArrayList<>();
            List<ChunkedFile> chunkedFiles = new ArrayList<>();

            for (File file : files) {
                if (file.isDirectory()) {
                    Files.walkFileTree(file.toPath(), new PathFileVisitor(currentPath, file, networkService, argsList, chunkedFiles));
                } else {
                    addPathToLists(argsList, chunkedFiles, currentPath.resolve(file.getName()), file);
                }
            }
            if (argsList.isEmpty()) {
                controllerService.setBlockedState(false, "Nothing to send.");
                return;
            }

            Command transf = new Command(KnownCommands.FileTransfer, argsList);
            networkService.sendCommand(transf);
            controllerService.setBlockedState(true, "Sending files...");

            prepare1ReadyCallback(transferHelperService, networkService, chunkedFiles);
            prepare2ReadyCallback(controllerService, transferHelperService);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void prepare2ReadyCallback(ControllerService controllerService, FileTransferHelperService transferHelperService) {
        transferHelperService.queueCommandCallback(KnownCommands.Ready, command -> {
            controllerService.setBlockedState(false, "Sending complete.");
            controllerService.localStructureRequest();
        });
    }

    private void prepare1ReadyCallback(FileTransferHelperService transferHelperService, NetworkService networkService, List<ChunkedFile> chunkedFiles) {
        transferHelperService.queueCommandCallback(KnownCommands.Ready, command -> {
            Factory.getPipelineManager().setup(PipelineSetup.FILE.handlers);
            for (ChunkedFile chunkedFile : chunkedFiles) {
                networkService.sendFile(chunkedFile);
            }
            Factory.getPipelineManager().setup(PipelineSetup.COMMAND.handlers);
        });
    }

    private void addPathToLists(List<String> argsList, List<ChunkedFile> chunkedFiles, Path relativePath, File file) throws IOException {
        ChunkedFile chunkedFile = new ChunkedFile(file);
        chunkedFiles.add(chunkedFile);
        argsList.add(relativePath.toString());
        argsList.add(String.valueOf(chunkedFile.length()));
    }

    private class PathFileVisitor extends SimpleFileVisitor<Path> {
        private final Path currentPath;
        private final File file;
        private final NetworkService networkService;
        private final List<String> argsList;
        private final List<ChunkedFile> chunkedFiles;

        public PathFileVisitor(Path currentPath, File file, NetworkService networkService, List<String> argsList, List<ChunkedFile> chunkedFiles) {
            this.currentPath = currentPath;
            this.file = file;
            this.networkService = networkService;
            this.argsList = argsList;
            this.chunkedFiles = chunkedFiles;
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            // Create empty directories first, non empty ones will be created with the actual files
            Path fileRelativePath = currentPath.resolve(file.toPath().getParent().relativize(dir).normalize());
            if (CommonUtil.isDirectoryEmpty(dir)) {
                Command createDir = new Command(KnownCommands.CreateDirectory, fileRelativePath.toString());
                networkService.sendCommand(createDir);
            }
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path visitedFile, BasicFileAttributes attrs) throws IOException {
            Path fileRelativePath = currentPath.resolve(file.toPath().getParent().relativize(visitedFile).normalize());
            addPathToLists(argsList, chunkedFiles, fileRelativePath, new File(visitedFile.toString()));
            return FileVisitResult.CONTINUE;
        }
    }
}
