package ru.home.cloud.file.manager.client.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;
import ru.home.cloud.file.manager.client.data.ResponseData;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class ResponseDataDecoder
    extends ReplayingDecoder<ResponseData> {
    private final Charset charset = StandardCharsets.UTF_8;

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf in, List<Object> out) throws Exception {
        ResponseData data = new ResponseData();
        data.setResIntValue(in.readInt());
        out.add(data);
    }
}
