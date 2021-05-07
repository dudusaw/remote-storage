package org.example;

import org.example.factory.Factory;

public class MainServer {

    public static void main(String[] args) throws Exception {
        Factory.getServerService().startServer();
    }
}
