import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;

public class NIOClient {
    private static final int BUFFER_SIZE = 256;
    private static final int PORT = 8188;

    public static void main(String[] args) {
        System.out.println("Starting NIO client!");
        String file = "C:\\Geekbrains\\cloud-file-manager\\client\\src\\main\\resources\\test.txt";

        try {
            InetSocketAddress myAddress = new InetSocketAddress(PORT);
            SocketChannel myClient = SocketChannel.open(myAddress);

            System.out.println("Connect to " + myAddress.getHostName() + " on port: " + myAddress.getPort());

            ByteBuffer myBuffer = ByteBuffer.allocate(BUFFER_SIZE);

            RandomAccessFile writer = new RandomAccessFile(file, "rw");
            FileChannel fileChannel = writer.getChannel();

            while (myClient.read(myBuffer) > 0) {
                myBuffer.flip();

                fileChannel.write(myBuffer);
                myBuffer.clear();
            }

            System.out.println("Close client connection!");
            myClient.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
