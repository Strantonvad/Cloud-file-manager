package ru.home.cloud.file.manager.common;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;

import java.nio.charset.StandardCharsets;

public class CommonActions {
    static void sendSignalByte(Channel ch, int signalByte){
        ByteBuf buf = null;
        buf = ByteBufAllocator.DEFAULT.directBuffer(1);
        buf.writeByte((byte) signalByte);
        ch.writeAndFlush(buf);
    }

    static void sendLength(Channel ch, int len){
        ByteBuf buf = null;
        buf = ByteBufAllocator.DEFAULT.directBuffer(4);
        buf.writeInt(len);
        ch.writeAndFlush(buf);
    }

    static void sendSize(Channel ch, long size){
        ByteBuf buf = null;
        buf = ByteBufAllocator.DEFAULT.directBuffer(8);
        buf.writeLong(size);
        ch.writeAndFlush(buf);
    }

    static void sendString(Channel ch, String str) {
        ByteBuf buf = null;
        byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
        buf = ByteBufAllocator.DEFAULT.directBuffer(bytes.length);
        buf.writeBytes(bytes);
        ch.writeAndFlush(buf);
    }
}
