package org.example.factory;

import org.example.service.*;
import org.example.service.impl.CommandProcessor;
import org.example.service.impl.FileTransferHelper;
import org.example.service.impl.NettyServerService;
import org.example.service.impl.PipelineManager;
import org.example.service.impl.ServerFileStorage;

import java.io.IOException;
import java.util.*;

public class Factory {

    private static Properties configProperties;

    static {
        constructProperties();
        FileTransferHelper.Instance.setFileStorageService(getStorageService());
    }

    public static ServerService getServerService() {
        return new NettyServerService();
    }

    public static FileStorageService getStorageService() {
        return ServerFileStorage.Instance;
    }

    public static Properties getConfigProperties() {
        return configProperties;
    }

    public static CommandProcessService getCommandProcessor() {
        return new CommandProcessor();
    }

    public static FileTransferHelperService getFileTransferService() {
        return FileTransferHelper.Instance;
    }

    public static PipelineManagerService getPipelineManager() {
        return PipelineManager.Instance;
    }

    private static void constructProperties() {
        try {
            configProperties = new Properties();
            configProperties.load(Factory.class.getClassLoader().getResourceAsStream("config/config.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
