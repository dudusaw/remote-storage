package org.example.client.service.impl;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.stream.ChunkedFile;
import org.example.domain.Command;
import org.example.client.factory.Factory;
import org.example.client.service.NetworkService;
import org.example.client.service.PipelineSetup;

import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

public class IONetworkService implements NetworkService {

    private IONetworkService() {}

    public static final NetworkService Instance = new IONetworkService();

    private SocketChannel socketChannel;

    @Override
    public void sendFile(ChunkedFile file) {
        socketChannel.writeAndFlush(file);
    }

    @Override
    public void sendCommand(Command cmd) {
        socketChannel.writeAndFlush(cmd);
    }

    @Override
    public void closeConnectionIfExists() {
        if (socketChannel != null) {
            socketChannel.close();
        }
    }

    @Override
    public void connectWithCallback(String host, int port, Consumer<Boolean> onResult) {
        assert socketChannel == null;

        new Thread(() -> connectToServerAndWait(host, port, onResult)).start();
    }

    private void connectToServerAndWait(String host, int port, Consumer<Boolean> onResult) {
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
                    Factory.getPipelineManager().setPipeline(ch.pipeline());
                    Factory.getPipelineManager().setup(PipelineSetup.COMMAND.handlers);
                }
            });

            // Start the client.
            ChannelFuture f = b.connect(host, port).addListener(future -> {
                onResult.accept(future.isSuccess());
            }).sync();

            System.out.println("Client connected");

            // Wait until the connection is closed.
            f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            workerGroup.shutdownGracefully();
        }
    }
}
