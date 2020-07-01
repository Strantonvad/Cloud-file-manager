package ru.home.cloud.file.manager.client.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import ru.home.cloud.file.manager.client.data.RequestData;
import ru.home.cloud.file.manager.client.data.ResponseData;

public class ClientHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelActive(ChannelHandlerContext ctx)
        throws Exception {

        RequestData msg = new RequestData();
        msg.setReqIntValue(123);
        msg.setReqStringValue(
            "all work and no play makes jack a dull boy");
        ctx.writeAndFlush(msg);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
        throws Exception {
        System.out.println((ResponseData)msg);
        ctx.close();
    }
}
