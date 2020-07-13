package ru.home.cloud.file.manager.common;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;

import java.nio.charset.StandardCharsets;

public class CommonActions {
    public static void sendFileList(String list, Channel channel) {
        sendSignalByte(channel);
        sendString(list, channel);
    }

    public static void sendSignalByte(Channel channel) {
        ByteBuf buf;
        buf = ByteBufAllocator.DEFAULT.directBuffer(1);
        buf.writeByte((byte) 11);
        channel.writeAndFlush(buf);
    }

    public static void sendString(String str, Channel channel) {
        ByteBuf buf;
        byte[] strBytes = str.getBytes(StandardCharsets.UTF_8);
        buf = ByteBufAllocator.DEFAULT.directBuffer(4);
        buf.writeInt(strBytes.length);
        channel.writeAndFlush(buf);
        buf = ByteBufAllocator.DEFAULT.directBuffer(strBytes.length);
        buf.writeBytes(strBytes);
        channel.writeAndFlush(buf);
    }
}
