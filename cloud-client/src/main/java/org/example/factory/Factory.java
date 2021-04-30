package org.example.factory;

import org.example.service.FileStorageService;
import org.example.service.FileTransferHelperService;
import org.example.service.PipelineManagerService;
import org.example.service.impl.FileTransferHelper;
import org.example.service.impl.PipelineManager;
import org.example.service.ControllerService;
import org.example.service.NetworkService;
import org.example.service.impl.ClientFileStorage;
import org.example.service.impl.IONetworkService;

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

    public static void setControllerService(ControllerService controllerService) {
        Factory.controllerService = controllerService;
    }
}
