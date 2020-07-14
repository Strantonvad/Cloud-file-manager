package ru.home.cloud.file.manager.client;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import ru.home.cloud.file.manager.common.ActionHandler;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;

public class NetworkClientHandler extends ChannelInboundHandlerAdapter {
    public enum State {
        WAIT, ACTION_LENGTH, ACTION, COMMAND_RECEIVED, STRING_LENGTH, STRING, NAME_LENGTH, NAME, FILE_LENGTH, FILE
//        IDLE,
//        COMMAND_LENGTH, COMMAND, COMMAND_HANDLE,
//        STRING_LENGTH, STRING,
//        NAME_LENGTH, NAME, FILE_LENGTH, FILE
    }

    private State currentState = State.WAIT;
    private Controller controller;
    private int currentLength;
    private long fileLength;
    private long receivedFileLength;
    private String receivedString;
    private String receivedCommand;
    private BufferedOutputStream out;

//    public NetworkClientHandler(Controller controller) {
//        this.controller = controller;
//    }
    NettyClient nettyClient = LoginController.getNettyClient();

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
                } else if (signalByte == (byte) 14) {
                    currentState = State.STRING_LENGTH;
                    System.out.println("Waiting for string");
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
                    byte[] commandBytes = new byte[currentLength];
                    buf.readBytes(commandBytes);
                    receivedCommand = new String(commandBytes, StandardCharsets.UTF_8);
                    System.out.println("Received action: " + receivedCommand);
                    currentState = State.COMMAND_RECEIVED;
                }
            }

            if (currentState == State.COMMAND_RECEIVED) {
                if (receivedCommand.equals("/update")) {
                    ActionHandler.sendAction("/list", nettyClient.getChannel());
                }
                receivedCommand = null;
                currentState = State.WAIT;
            }

            if (currentState == State.STRING_LENGTH) {
                if (buf.readableBytes() >= 4) {
                    System.out.println("Get string length");
                    currentLength = buf.readInt();
                    currentState = State.STRING;
                }
            }

            if (currentState == State.STRING) {
                if (buf.readableBytes() >= currentLength) {
                    byte[] stringBytes = new byte[currentLength];
                    buf.readBytes(stringBytes);
                    receivedString = new String(stringBytes, StandardCharsets.UTF_8);
                    System.out.println("String received: " + receivedString);
                    controller.setCloudStorageFileList(receivedString);
                    currentState = State.WAIT;
                }
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
//                    String fileName = new String(fileNameBytes, StandardCharsets.UTF_8);
//                    System.out.println("STATE: Filename received - _" + new String(fileNameBytes, StandardCharsets.UTF_8));
//                    out = new BufferedOutputStream(new FileOutputStream("./client-files/" + new String(fileNameBytes)));
                    String fileName = new String(fileNameBytes, StandardCharsets.UTF_8);
                    System.out.println("Received filename: " + fileName);
                    out = new BufferedOutputStream(new FileOutputStream("./client-files/" + fileName));
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
                    out.write(buf.readByte());
                    receivedFileLength++;
                    if (fileLength == receivedFileLength) {
                        currentState = State.WAIT;
                        System.out.println("File received");
                        controller.updateClientStorageFileList();
                        out.close();
                        break;
                    }
                }
            }
        }
        if (buf.readableBytes() == 0) {
            buf.release();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
