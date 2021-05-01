package org.example.client;

// Some bug in javafx, that we can't launch from a main method which is inside Application class, so we need another class for launch
public class MainClient {

    public static void main(String[] args) {
        App.main(args);
    }
}
