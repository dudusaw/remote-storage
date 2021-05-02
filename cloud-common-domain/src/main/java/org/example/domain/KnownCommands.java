package org.example.domain;

public enum KnownCommands {
    Ready("/ready"),
    Fail("/fail"),
    FileTransfer("/transf"),
    FileRequest("/reqf"),
    CreateDirectory("/cdir"),
    Delete("/delete"),
    FileStructureRequest("/reqfs"),
    LocalStructureRequest("/reqlocal"),
    RegisterRequest("/reg"),
    LoginRequest("/lg"),
    Move("/mv");

    public final String name;

    KnownCommands(String name) {
        this.name = name;
    }
}
