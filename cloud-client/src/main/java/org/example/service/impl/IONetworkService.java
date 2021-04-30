package org.example.service.impl;

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
import org.example.factory.Factory;
import org.example.service.NetworkService;
import org.example.service.PipelineSetup;

import java.util.concurrent.CountDownLatch;

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
    public void connect(String host, int port) {
        assert socketChannel == null;

        CountDownLatch latch = new CountDownLatch(1);

        new Thread(() -> connectToServerAndWait(host, port, latch)).start();

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setupPipeline(PipelineSetup setup) {
        Factory.getPipelineManager().setup(setup.handlers);
    }

    private void connectToServerAndWait(String host, int port, CountDownLatch latch) {
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
            ChannelFuture f = b.connect(host, port).sync(); // (5)

            System.out.println("Client connected");

            latch.countDown();

            // Wait until the connection is closed.
            f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            workerGroup.shutdownGracefully();
        }
    }
}
