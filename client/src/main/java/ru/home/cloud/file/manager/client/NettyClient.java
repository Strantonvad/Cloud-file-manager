package ru.home.cloud.file.manager.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

public class NettyClient {
    //    private SocketChannel channel;
    private static final int PORT = 8888;
    private static final String HOST = "localhost";

    public NettyClient() {
        Thread t = new Thread(() -> {
            EventLoopGroup workerGroup = new NioEventLoopGroup();
            try {
                Bootstrap b = new Bootstrap();
                b.group(workerGroup)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(
//                                new RequestDataEncoder(),
//                                new ResponseDataDecoder(),
                                new NetworkClientHandler());
                        }
                    });

                ChannelFuture f = b.connect(HOST, PORT).sync();
//                f.channel().closeFuture().sync();
                BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
                String line;
                while ((line = reader.readLine()) != null) {
                    ByteBuf byteBuf = Unpooled.buffer();
                    if ((line = line.toLowerCase()).length() == 0) {
                        continue;
                    }
                    if (line.startsWith("ping")) {
                        byteBuf.writeInt(0);
                        byteBuf.writeLong(System.nanoTime());
                    }
                    if (line.startsWith("copy")) {
                        byteBuf.writeInt(1);
                        File file = new File("C:\\Geekbrains\\cloud-file-manager\\client\\src\\main\\resources\\test.txt");
                        byteBuf.writeBytes(new FileInputStream(file), (int) file.length());
                    }
                    f.channel().writeAndFlush(byteBuf, f.channel().voidPromise());
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                workerGroup.shutdownGracefully();
            }
        });
        t.start();
    }

    public static void main(String[] args) {
        new NettyClient();
    }

}
