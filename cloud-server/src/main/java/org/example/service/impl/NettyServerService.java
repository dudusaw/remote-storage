package org.example.service.impl;

import org.example.domain.Command;
import org.example.factory.Factory;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.example.service.DBService;
import org.flywaydb.core.Flyway;
import org.example.service.PipelineSetup;
import org.example.service.ServerService;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

public class NettyServerService implements ServerService {

    @Override
    public void startServer() {
        setupMigrations();
        startServerAndWait();
    }

    private void setupMigrations() {
        Properties prop = Factory.getConfigProperties();
        String url = prop.getProperty("dbUrl");
        String user = prop.getProperty("dbUser");
        String password = prop.getProperty("dbPassword");
        Flyway flyway = Flyway.configure().dataSource(url, user, password).load();
        flyway.repair();
        flyway.migrate();
    }

    private void startServerAndWait() {
        Factory.getDbService().connectDBFromConfig();

        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel channel) {
                            Factory.getPipelineManager().setPipeline(channel.pipeline());
                            Factory.getPipelineManager().setup(PipelineSetup.COMMAND.handlers);
                        }
                    });
            Properties prop = Factory.getConfigProperties();
            String host = prop.getProperty("ServerHost");
            int port = Integer.parseInt(prop.getProperty("ServerPort"));

            ChannelFuture future = bootstrap.bind(host, port).sync();
            System.out.println("Server started");
            future.channel().closeFuture().sync(); // block
        } catch (Exception e) {
            System.out.println("Server shutdown");
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            Factory.getDbService().closeConnection();
        }
    }

}