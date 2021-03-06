package org.example.service.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.example.domain.Command;
import org.example.domain.KnownCommands;
import org.example.factory.Factory;
import org.example.service.FileTransferHelperService;
import org.example.service.PipelineSetup;

public class FileInboundHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf byteBuf = ((ByteBuf) msg);
        FileTransferHelperService transferHelperService = Factory.getFileTransferService();
        boolean finished = transferHelperService.readBytes(byteBuf);
        byteBuf.release();

        if (finished) {
            Factory.getPipelineManager().setup(PipelineSetup.COMMAND.handlers);
            ctx.writeAndFlush(new Command(KnownCommands.Ready)).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
        }
    }
}
