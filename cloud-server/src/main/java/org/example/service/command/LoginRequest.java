package org.example.service.command;

import io.netty.channel.ChannelHandlerContext;
import org.example.domain.Command;
import org.example.domain.KnownCommands;
import org.example.factory.Factory;
import org.example.service.CommandService;

import java.sql.SQLException;

public class LoginRequest implements CommandService {
    @Override
    public void processCommand(ChannelHandlerContext ctx, Command command) {
        String login = command.getArgs()[0];
        String password = command.getArgs()[1];
        try {
            boolean success = Factory.getDbService().tryLogin(login, password);
            if (success) {
                ctx.writeAndFlush(new Command(KnownCommands.Ready));
            } else {
                ctx.writeAndFlush(new Command(KnownCommands.Fail, "Wrong login/password."));
            }
        } catch (SQLException throwables) {
            System.err.println(throwables.getMessage());
            ctx.writeAndFlush(new Command(KnownCommands.Fail, throwables.getMessage()));
        }
    }

    @Override
    public KnownCommands getCommand() {
        return KnownCommands.LoginRequest;
    }
}
