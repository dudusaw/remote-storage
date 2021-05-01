package org.example.factory;

import org.example.service.*;
import org.example.service.impl.*;

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
