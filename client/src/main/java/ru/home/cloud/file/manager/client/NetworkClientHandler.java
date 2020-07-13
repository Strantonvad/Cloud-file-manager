package ru.home.cloud.file.manager.client;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

public class NetworkClientHandler extends ChannelInboundHandlerAdapter {
    private State currentState = State.WAIT;
    private int nextLength;
    private long fileLength;
    private long receivedFileLength;
    private BufferedOutputStream out;
    private Path clientPath;
    private String filesList;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Client connect :" + ctx.name());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = ((ByteBuf) msg);
        while (buf.readableBytes() > 0) {
            if (currentState == State.WAIT) {
                byte currentByte = buf.readByte();
                if (currentByte == (byte) 13) {
                    currentState = State.NAME_LENGTH;
                    receivedFileLength = 0L;
                    System.out.println("Сигнальный байт = " + currentByte + " (копирование файла)");
                } else if (currentByte == (byte) 11) {
                    currentState = State.FILE_LIST_LENGTH;
                    receivedFileLength = 0L;
                    System.out.println("Сигнальный байт = " + currentByte + " (получение списка файлов)");
                } else if (currentByte == (byte) 12) {
                    currentState = State.NAME_LENGTH;
                    receivedFileLength = 0L;
                    System.out.println("Сигнальный байт = " + currentByte + " (копирование файла с сервера)");
                } else {
                    System.out.println("Не корректный сигнальный байт: " + currentByte);
                }
            }

            if (currentState == State.FILE_LIST_LENGTH) {
                if (buf.readableBytes() >= 4) {
                    nextLength = buf.readInt();
                    System.out.println("Длинна списка файлов = " + nextLength);
                    currentState = State.FILE_LIST;
                }
            }

            if (currentState == State.FILE_LIST) {
                if (buf.readableBytes() >= nextLength) {
                    byte[] clientsListBuf = new byte[nextLength];
                    buf.readBytes(clientsListBuf);
                    filesList = new String(clientsListBuf, StandardCharsets.UTF_8);
                    System.out.println("Получаем список файлов =" + filesList);
                    currentState = State.WAIT;
                }
            }

            if (currentState == State.NAME_LENGTH) {
                if (buf.readableBytes() >= 4) {
                    nextLength = buf.readInt();
                    System.out.println("Получение длинны имени файла = " + nextLength);
                    currentState = State.NAME;
                }
            }

            if (currentState == State.NAME) {
                if (buf.readableBytes() >= nextLength) {
                    byte[] fileName = new byte[nextLength];
                    buf.readBytes(fileName);
                    String currentFileName = new String(fileName, StandardCharsets.UTF_8);
                    System.out.println("Имя файла: " + currentFileName);
                    out = new BufferedOutputStream(new FileOutputStream("./" + currentFileName));
                    currentState = State.FILE_LENGTH;
                }
            }

            if (currentState == State.FILE_LENGTH) {
                if (buf.readableBytes() >= 8) {
                    fileLength = buf.readLong();
                    System.out.println("Длина файла: " + fileLength);
                    currentState = State.FILE;
                }
            }

            if (currentState == State.FILE) {
                while (buf.readableBytes() > 0) {
                    out.write(buf.readByte());
                    receivedFileLength++;
                    if (fileLength == receivedFileLength) {
                        currentState = State.WAIT;
                        System.out.println("Файл получен");
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

    public enum State {
        WAIT, NAME_LENGTH, NAME, FILE_LENGTH, FILE, FILE_LIST_LENGTH, FILE_LIST
    }
}
