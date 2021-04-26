package org.example.factory;

import org.example.service.NetworkService;
import org.example.service.impl.IONetworkService;

public class Factory {

    public static NetworkService getNetworkService() {
        return IONetworkService.Instance;
    }
}
