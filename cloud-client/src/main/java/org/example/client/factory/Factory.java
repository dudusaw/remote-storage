package org.example.client.factory;

import org.example.client.service.*;
import org.example.client.service.impl.*;
import org.example.service.CommandProcessService;
import org.example.service.FileStorageService;
import org.example.service.FileTransferHelperService;
import org.example.service.PipelineManagerService;
import org.example.service.impl.FileTransferHelper;
import org.example.service.impl.PipelineManager;

public class Factory {

    private static ControllerService controllerService;

    static {
        FileTransferHelper.Instance.setFileStorageService(ClientFileStorage.Instance);
    }

    public static NetworkService getNetworkService() {
        return IONetworkService.Instance;
    }

    public static FileStorageService getFileStorageService() {
        return ClientFileStorage.Instance;
    }

    public static FileTransferHelperService getFileTransferService() {
        return FileTransferHelper.Instance;
    }

    public static PipelineManagerService getPipelineManager() {
        return PipelineManager.Instance;
    }

    public static ControllerService getControllerService() {
        return controllerService;
    }

    public static CommandProcessService getCommandProcessor() {
        return ClientCommandProcessor.Instance;
    }

    public static void setControllerService(ControllerService controllerService) {
        Factory.controllerService = controllerService;
    }
}
