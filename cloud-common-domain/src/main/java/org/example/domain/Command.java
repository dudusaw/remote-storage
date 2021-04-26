package org.example.domain;

import java.io.Serializable;

public class Command implements Serializable {

    private String commandName;
    private String[] args;

    public Command(KnownCommands name, String... args) {
        this.commandName = name.name;
        this.args = args;
    }

    public String getCommandName() {
        return commandName;
    }

    public String[] getArgs() {
        return args;
    }
}