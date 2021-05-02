package org.example.client.service.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.example.domain.Command;
import org.example.domain.KnownCommands;
import org.example.service.FileTransferHelperService;
import org.example.client.factory.Factory;
import org.example.client.service.PipelineSetup;

public class ClientFileInboundHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf byteBuf = ((ByteBuf) msg);
        FileTransferHelperService transferHelperService = Factory.getFileTransferService();
        boolean finished = transferHelperService.readBytes(byteBuf);
        byteBuf.release();

        if (finished) {
            Factory.getPipelineManager().setup(PipelineSetup.COMMAND.handlers);
            ctx.writeAndFlush(new Command(KnownCommands.Ready));
            Factory.getFileTransferService().queueCommandCallback(KnownCommands.Ready, command ->
                    Factory.getControllerService().setBlockedState(false, "Download complete."));
        }
    }
}
