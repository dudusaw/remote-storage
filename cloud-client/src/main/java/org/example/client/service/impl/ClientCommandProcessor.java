package org.example.client.service.impl;

import io.netty.channel.ChannelHandlerContext;
import org.example.domain.Command;
import org.example.service.CommandProcessService;
import org.example.service.CommandService;
import org.reflections8.Reflections;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class ClientCommandProcessor implements CommandProcessService {

    private final Map<String, CommandService> commandDictionary;

    private ClientCommandProcessor() {
        commandDictionary = Collections.unmodifiableMap(getCommonDictionary());
    }

    public static CommandProcessService Instance = new ClientCommandProcessor();

    private Map<String, CommandService> getCommonDictionary() {
        var commandServiceList = constructCommandServiceList();

        Map<String, CommandService> commandDictionary = new HashMap<>();
        for (CommandService commandService : commandServiceList) {
            commandDictionary.put(commandService.getCommand().name, commandService);
        }

        return commandDictionary;
    }

    @Override
    public void processCommand(ChannelHandlerContext ctx, Command command) {
        commandDictionary.get(command.getCommandName()).processCommand(ctx, command);
    }

    private static List<CommandService> constructCommandServiceList() {
        Reflections reflections = new Reflections("org.example.client.service.command");

        var services = reflections.getSubTypesOf(CommandService.class);
        if (services.isEmpty()) {
            throw new IllegalStateException("No commands found.");
        }
        List<CommandService> result = new ArrayList<>();
        try {
            for (var serviceClass : services) {
                CommandService newService = serviceClass.getDeclaredConstructor().newInstance();
                result.add(newService);
            }
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return result;
    }
}
