package org.example.service.impl;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.example.service.NetworkService;

public class IONetworkService implements NetworkService {

    private IONetworkService() {}

    public static final NetworkService Instance = new IONetworkService();

    private SocketChannel socketChannel;

    @Override
    public void sendCommand(String command) {

    }

    @Override
    public int readCommandResult(byte[] buffer) {
        return 0;
    }

    @Override
    public void closeConnectionIfExists() {
        if (socketChannel != null) {
            socketChannel.close();
        }
    }

    @Override
    public void connect(String host, int port) {
        assert socketChannel == null;

        new Thread(() -> connectToServerAndWait(host, port)).start();
    }

    private void connectToServerAndWait(String host, int port) {
        EventLoopGroup workerGroup = new NioEventLoopGroup(1);

        try {
            Bootstrap b = new Bootstrap();
            b.group(workerGroup);
            b.channel(NioSocketChannel.class);
            b.option(ChannelOption.SO_KEEPALIVE, true);
            b.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) {
                    IONetworkService.this.socketChannel = ch;
                    // pipeline
                }
            });

            // Start the client.
            ChannelFuture f = b.connect(host, port).sync(); // (5)

            // Wait until the connection is closed.
            f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            workerGroup.shutdownGracefully();
        }
    }
}
