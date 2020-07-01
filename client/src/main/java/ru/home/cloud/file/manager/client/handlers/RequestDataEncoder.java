package ru.home.cloud.file.manager.client.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import ru.home.cloud.file.manager.client.data.RequestData;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class RequestDataEncoder extends MessageToByteEncoder<RequestData> {

    private final Charset charset = StandardCharsets.UTF_8;

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, RequestData msg, ByteBuf byteBuf) throws Exception {
        byteBuf.writeInt(msg.getReqIntValue());
        byteBuf.writeInt(msg.getReqStringValue().length());
        byteBuf.writeCharSequence(msg.getReqStringValue(), charset);
    }
}
