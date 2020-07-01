package ru.home.cloud.file.manager.client;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;

import java.util.concurrent.ConcurrentLinkedDeque;

public class MainHandler extends ChannelInboundHandlerAdapter {
    static ConcurrentLinkedDeque<ChannelHandlerContext> clients = new ConcurrentLinkedDeque<>();
    static int cnt = 0;
    private int id;
    private String userName;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        clients.add(ctx);
        cnt++;
        id = cnt;
        userName = "user#" + id;
        System.out.println("Client connected! " + userName);

    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buffer = (ByteBuf) msg;
        StringBuilder message = new StringBuilder();
        while (buffer.readableBytes() > 0) {
            message.append((char) buffer.readByte());
        }
        System.out.println(message.toString().replaceAll("\n", ""));
        ctx.writeAndFlush(Unpooled.copiedBuffer(userName + ": " + message, CharsetUtil.UTF_8));
//        for(ChannelHandlerContext client : clients) {
//            client.writeAndFlush(Unpooled.copiedBuffer(userName + ": " + message, CharsetUtil.UTF_8));
//        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Client disconnected!");
        clients.remove(ctx);
        cnt--;
    }
}
