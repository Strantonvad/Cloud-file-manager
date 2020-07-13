package ru.home.cloud.file.manager.common;

import io.netty.channel.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ActionHandler {
    private static int SEND_FILE_BYTE = 12;
    private static int SEND_ACTION_BYTE = 13;
    private static int SEND_STRING_BYTE = 14;

    public static void sendStringData(String str, Channel ch) {
        CommonActions.sendSignalByte(ch, SEND_STRING_BYTE);
        CommonActions.sendLength(ch, str.length());
        CommonActions.sendString(ch, str);
    }

    public static void sendFile(Path path, Channel ch, ChannelFutureListener listener) throws IOException {
        FileRegion region = new DefaultFileRegion(path.toFile(), 0, Files.size(path));

        CommonActions.sendSignalByte(ch, SEND_FILE_BYTE);
        CommonActions.sendLength(ch, path.getFileName().toString().length());
        CommonActions.sendString(ch, path.getFileName().toString());
        CommonActions.sendSize(ch, Files.size(path));

        ChannelFuture channelFuture = ch.writeAndFlush(region);
        if (listener != null) {
            channelFuture.addListener(listener);
        }
    }

    public static void sendAction(String action, Channel ch) {
        CommonActions.sendSignalByte(ch, SEND_ACTION_BYTE);
        CommonActions.sendLength(ch, action.length());
        CommonActions.sendString(ch, action);
    }
}
