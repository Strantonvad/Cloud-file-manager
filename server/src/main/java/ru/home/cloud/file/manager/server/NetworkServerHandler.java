package ru.home.cloud.file.manager.server;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import ru.home.cloud.file.manager.common.ActionHandler;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class NetworkServerHandler extends ChannelInboundHandlerAdapter {
    private State currentState = State.WAIT;
    private long receivedFileLength;
    private int currentLength;
    private String currentAction;
    private BufferedOutputStream output;
    private long fileLength;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object obj) throws Exception {
        ByteBuf buf = ((ByteBuf) obj);
        while (buf.readableBytes() > 0) {
            if (currentState == State.WAIT) {
                byte signalByte = buf.readByte();
                if (signalByte == (byte) 12) {
                    currentState = State.NAME_LENGTH;
                    receivedFileLength = 0L;
                    System.out.println("File receiving started");

                } else if (signalByte == (byte) 13) {
                    currentState = State.ACTION_LENGTH;
                    System.out.println("Waiting for action");
                } else {
                    System.out.println("Signal byte " + signalByte + " - incorrect");
                }
            }

            if (currentState == State.ACTION_LENGTH) {
                if (buf.readableBytes() >= 4) {
                    System.out.println("Get action length");
                    currentLength = buf.readInt();
                    currentState = State.ACTION;
                }
            }

            if (currentState == State.ACTION) {
                if (buf.readableBytes() >= currentLength) {
                    byte[] actionBytes = new byte[currentLength];
                    buf.readBytes(actionBytes);
                    currentAction = new String(actionBytes, StandardCharsets.UTF_8);
                    System.out.println("Received action: " + currentAction);
                    currentState = State.COMMAND_RECEIVED;
                }
            }

            if (currentState == State.COMMAND_RECEIVED) {
                if (currentAction.contains("/list")) {
//                    List<String> filesList = LocalUtils.getFileListFromDirectory(Paths.get("./server-files"));
                    List<String> filesList = Files.list(Paths.get("./server-files")).
                        filter(Files::isRegularFile).
                        map(o -> o.getFileName().toString()).
                        collect(Collectors.toList());

                    ActionHandler.sendStringData(String.join(";", filesList), ctx.channel());
                }
                if (currentAction.contains("/file")) {
                    String file = currentAction.split(" ")[1];
                    ActionHandler.sendFile(Paths.get("./server-files/" + file), ctx.channel(), future -> {
                        if (future.isSuccess()) {
                            System.out.println("File " + file + " successfully send");
                        }
                    });
                }
                if (currentAction.contains("/delete")) {
                    String deletedFile = currentAction.split(" ")[1];
                    Files.deleteIfExists(Paths.get("./server-files/" + deletedFile));
                    ActionHandler.sendAction("/update", ctx.channel());
                }
                currentAction = null;
                currentState = State.WAIT;
            }

            if (currentState == State.NAME_LENGTH) {
                if (buf.readableBytes() >= 4) {
                    currentLength = buf.readInt();
                    System.out.println("Filename length: " + currentLength);
                    currentState = State.NAME;
                }
            }

            if (currentState == State.NAME) {
                if (buf.readableBytes() >= currentLength) {
                    byte[] fileNameBytes = new byte[currentLength];
                    buf.readBytes(fileNameBytes);
                    String fileName = new String(fileNameBytes, StandardCharsets.UTF_8);
                    System.out.println("Received filename: " + fileName);
                    output = new BufferedOutputStream(new FileOutputStream("./server-files/" + fileName));
                    currentState = State.FILE_LENGTH;
                }
            }

            if (currentState == State.FILE_LENGTH) {
                if (buf.readableBytes() >= 8) {
                    fileLength = buf.readLong();
                    System.out.println("File length :" + fileLength);
                    currentState = State.FILE;
                }
            }

            if (currentState == State.FILE) {
                while (buf.readableBytes() > 0) {
                    output.write(buf.readByte());
                    receivedFileLength++;
                    if (fileLength == receivedFileLength) {
                        currentState = State.WAIT;
                        System.out.println("File received");
                        output.close();
                        break;
                    }
                }
            }
        }
        if (buf.readableBytes() == 0) {
            buf.release();
        }
    }

    public enum State {
        WAIT, ACTION_LENGTH, ACTION, COMMAND_RECEIVED, NAME_LENGTH, NAME, FILE_LENGTH, FILE
    }
}
