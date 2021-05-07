package org.example.service.command;

import io.netty.channel.ChannelHandlerContext;
import org.example.domain.Command;
import org.example.domain.KnownCommands;
import org.example.factory.Factory;
import org.example.service.CommandService;
import org.example.service.DBService;

import java.sql.SQLException;

public class RegisterRequest implements CommandService {
    @Override
    public void processCommand(ChannelHandlerContext ctx, Command command) {
        DBService dbService = Factory.getDbService();
        String login = command.getArgs()[0];
        String password = command.getArgs()[1];
        if (login.length() < 3 || password.length() < 3) {
            ctx.writeAndFlush(new Command(KnownCommands.Fail, "Login or password is too short (min 3 symbols)."));
            return;
        }
        try {
            dbService.createUser(login, password);
            ctx.writeAndFlush(new Command(KnownCommands.Ready));
        } catch (SQLException throwables) {
            System.err.println(throwables.getMessage());
            ctx.writeAndFlush(new Command(KnownCommands.Fail, throwables.getMessage()));
        }
    }

    @Override
    public KnownCommands getCommand() {
        return KnownCommands.RegisterRequest;
    }
}
