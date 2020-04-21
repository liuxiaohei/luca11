package org.ld.examples.nio.socket;

import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class SocketTest {
    @Test
    public void BSocket() throws IOException {
        int port = 8987;
        new Thread(() -> {
            try {
                Thread.sleep(2000);
                Socket socket = new Socket("localhost", port);
                InputStream in = socket.getInputStream();
                byte[] b = new byte[1024];
                int len = -1;
                while ((len = in.read(b)) != -1) {
                    System.out.println(new String(b, 0, len));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
        BSocket bSocket = new BSocket();
        bSocket.serve(port);
    }

    @Test
    public void NSocket() throws IOException {
        int port = 8988;
        new Thread(() -> {
            try {
                Thread.sleep(2000);
                SocketChannel channel = SocketChannel.open(new InetSocketAddress("localhost",port));
                ByteBuffer buffer = ByteBuffer.allocate(1024);
                channel.read(buffer);
                buffer.flip();
                System.out.println(new String(buffer.array(),0,buffer.limit()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        NSocket nSocket = new NSocket();
        nSocket.serve(port);
    }
}
