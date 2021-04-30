package org.example.service.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.example.service.FileTransferHelperService;
import org.example.factory.Factory;
import org.example.service.PipelineSetup;

public class ClientFileInboundHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf byteBuf = ((ByteBuf) msg);
        FileTransferHelperService transferHelperService = Factory.getFileTransferService();
        boolean finished = transferHelperService.readBytes(byteBuf);
        byteBuf.release();

        if (finished) {
            Factory.getPipelineManager().setup(PipelineSetup.COMMAND.handlers);
            Factory.getControllerService().setBlockedState(false, "File transfer complete.");
        }
    }
}