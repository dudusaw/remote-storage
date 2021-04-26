package org.example.factory;

import org.example.domain.service.FileStorageService;
import org.example.domain.service.FileTransferHelperService;
import org.example.domain.service.impl.FileTransferHelper;
import org.example.service.NetworkService;
import org.example.service.impl.ClientFileStorage;
import org.example.service.impl.IONetworkService;

public class Factory {

    static {
        FileTransferHelper.Instance.setFileStorageService(ClientFileStorage.Instance);
    }

    public static NetworkService getNetworkService() {
        return IONetworkService.Instance;
    }

    public static FileStorageService getFileStorageService() {
        return ClientFileStorage.Instance;
    }

    public static FileTransferHelperService getFileTransferHelper() {
        return FileTransferHelper.Instance;
    }
}
