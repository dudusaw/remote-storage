package org.example.domain;

import java.io.Serializable;
import java.util.List;

public class Command implements Serializable {

    private String commandName;
    private String[] args;

    public Command(KnownCommands name, String... args) {
        this.commandName = name.name;
        this.args = args;
    }

    public Command(KnownCommands name, List<String> args) {
        this.commandName = name.name;
        String[] a = new String[args.size()];
        args.toArray(a);
        this.args = a;
    }

    public String getCommandName() {
        return commandName;
    }

    public String[] getArgs() {
        return args;
    }
}