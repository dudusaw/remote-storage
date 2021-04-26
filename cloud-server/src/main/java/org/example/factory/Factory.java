package org.example.factory;

import org.example.domain.service.FileStorageService;
import org.example.domain.service.FileTransferHelperService;
import org.example.service.*;
import org.example.service.impl.CommandProcessor;
import org.example.domain.service.impl.FileTransferHelper;
import org.example.service.impl.NettyServerService;
import org.example.service.impl.ServerFileStorage;
import org.reflections8.Reflections;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
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

    private static void constructProperties() {
        try {
            configProperties = new Properties();
            configProperties.load(Factory.class.getClassLoader().getResourceAsStream("config/config.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
