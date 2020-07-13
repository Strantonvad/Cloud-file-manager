package ru.home.cloud.file.manager.server;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import ru.home.cloud.file.manager.common.CommonActions;
import ru.home.cloud.file.manager.common.FileActions;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class NetworkServerHandler extends ChannelInboundHandlerAdapter {
    private State currentState = State.WAIT;
    private int nextLength;
    private long fileLength;
    private long receivedFileLength;
    private BufferedOutputStream out;
    private Path clientPath;
    private String receivedCommand;
    private final String nettyServerPath = NettyServer.getServerPath().normalize().toAbsolutePath().toString();

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Client connect :" + ctx);

        clientPath = Paths.get(nettyServerPath + "\\server-files");
        if (!Files.exists(clientPath)) {
            System.out.println("New client path: " + clientPath);
            Files.createDirectory(clientPath);
        }
        NettyServer.setServerPath(clientPath);
        CommonActions.sendFileList(clientPath.toString(), ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Client disconnected.");
        ctx.close();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = ((ByteBuf) msg);
        while (buf.readableBytes() > 0) {
            if (currentState == State.WAIT) {
                byte currentByte = buf.readByte();
                //get file
                if (currentByte == 12) {
                    currentState = State.NAME_LENGTH;
                    receivedFileLength = 0L;
                    System.out.println("Signal byte: " + currentByte + ". Copy file");
                    //get command
                } else if (currentByte == 13) {
                    currentState = State.COMMAND_LENGTH;
                    System.out.println("Waiting for command");
                } else {
                    System.out.println("Invalid signal byte: " + currentByte);
                }
            }

            if (currentState == State.COMMAND_LENGTH) {
                if (buf.readableBytes() >= 4) {
                    System.out.println("Get command length");
                    nextLength = buf.readInt();
                    currentState = State.COMMAND;
                }
            }

            if (currentState == State.COMMAND) {
                if (buf.readableBytes() >= nextLength) {
                    byte[] commandBytes = new byte[nextLength];
                    buf.readBytes(commandBytes);
                    receivedCommand = new String(commandBytes, StandardCharsets.UTF_8);
                    System.out.println("Command received: " + receivedCommand);
                    currentState = State.COMMAND_HANDLE;
                }
            }

            if (currentState == State.COMMAND_HANDLE) {
                if (receivedCommand.contains("/file")) {
                    String requestedFile = receivedCommand.split(" ")[1];
                    FileActions.sendFile(Paths.get("./server-files/" + requestedFile), ctx.channel(), future -> {
                        if (future.isSuccess()) {
                            System.out.println("File " + requestedFile + " transferred");
                        }
                    });
                }
                receivedCommand = null;
                currentState = State.WAIT;
            }

            if (currentState == State.NAME_LENGTH) {
                if (buf.readableBytes() >= 4) {
                    System.out.println("Get file name length");
                    nextLength = buf.readInt();
                    currentState = State.NAME;
                }
            }

            if (currentState == State.NAME) {
                if (buf.readableBytes() >= nextLength) {
                    byte[] fileName = new byte[nextLength];
                    buf.readBytes(fileName);
                    String currentFileName = new String(fileName, StandardCharsets.UTF_8);
                    System.out.println("Write file :" + currentFileName);
                    out = new BufferedOutputStream(new FileOutputStream(clientPath + "\\" + currentFileName));
                    currentState = State.FILE_LENGTH;
                }
            }

            if (currentState == State.FILE_LENGTH) {
                if (buf.readableBytes() >= 8) {
                    fileLength = buf.readLong();
                    System.out.println("Got file length: " + fileLength);
                    currentState = State.FILE;
                }
            }

            if (currentState == State.FILE) {
                while (buf.readableBytes() > 0) {
                    out.write(buf.readByte());
                    receivedFileLength++;
                    if (fileLength == receivedFileLength) {
                        currentState = State.WAIT;
                        System.out.println("File received!");
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

    public enum State {
        WAIT, NAME_LENGTH, NAME, FILE_LENGTH, FILE, COMMAND_LENGTH, COMMAND, COMMAND_HANDLE
    }
}
