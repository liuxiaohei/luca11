package org.ld.nio;

import org.junit.jupiter.api.Test;
import org.ld.exception.CodeStackException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

public class SocketTest {

    /**
     * Biotesk
     */
    @Test
    public void BSocket() {
        var port = 8987;
        new Thread(() -> {
            try {
                Thread.sleep(2000);
                var socket = new Socket("localhost", port);
                var in = socket.getInputStream();
                var b = new byte[1024];
                int len;
                while ((len = in.read(b)) != -1) { // 阻塞2
                    System.out.println(new String(b, 0, len));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
        try {
            final var socket = new ServerSocket(port);
            while (true) {
                new Thread(() -> {
                    OutputStream out;
                    try (final var clientSocket = socket.accept()) { // 阻塞1
                        out = clientSocket.getOutputStream();
                        System.out.println("Accepted connection from " + clientSocket);
                        out.write("Hi!\r\n".getBytes(StandardCharsets.UTF_8));                            //4
                        out.flush();
                    } catch (IOException e) {
                        throw new CodeStackException(e);
                    }
                }).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * NioTest
     */
    @Test
    public void NSocket() throws IOException {
        var port = 8988;
        new Thread(() -> {
            try {
                Thread.sleep(2000);
                var channel = SocketChannel.open(new InetSocketAddress("localhost", port));
                var buffer = ByteBuffer.allocate(1024);
                channel.read(buffer);
                buffer.flip();
                System.out.println(new String(buffer.array(), 0, buffer.limit()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        var serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);
        var ss = serverChannel.socket();
        var address = new InetSocketAddress(port);
        ss.bind(address);
        var selector = Selector.open();
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);
        final var msg = ByteBuffer.wrap("Hi!\r\n".getBytes());
        while (true) {
            try {
                selector.select();
            } catch (IOException ex) {
                ex.printStackTrace();
                break;
            }
            var readyKeys = selector.selectedKeys();
            var iterator = readyKeys.iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                iterator.remove();
                try {
                    if (key.isAcceptable()) {
                        var server = (ServerSocketChannel) key.channel();
                        var client = server.accept();
                        client.configureBlocking(false);
                        client.register(selector, SelectionKey.OP_WRITE | SelectionKey.OP_READ, msg.duplicate());    //7
                        System.out.println("Accepted connection from " + client);
                    }
                    if (key.isWritable()) {
                        try (var client = (SocketChannel) key.channel()) {
                            var buffer = (ByteBuffer) key.attachment();
                            while (buffer.hasRemaining()) {
                                if (client.write(buffer) == 0) {
                                    break;
                                }
                            }
                        }
                    }
                } catch (IOException ex) {
                    key.cancel();
                    try {
                        key.channel().close();
                    } catch (IOException ignored) {
                    }
                }
            }
        }
    }

    /**
     * AioTest
     */
    @Test
    public void ASocket() throws IOException {
        new Thread(() -> {
            try {
                Thread.sleep(1000);
                var socket = new Socket("localhost", 8887);
                var reader = new BufferedReader(new InputStreamReader(System.in));
                var line = reader.readLine();
                socket.getOutputStream().write(line.getBytes());
                var resp = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                System.out.println(" 接收到的响应 " + resp.readLine());
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.exit(1);
        }).start();

        //ChannelGroup用来管理共享资源
        var group = AsynchronousChannelGroup.withCachedThreadPool(Executors.newCachedThreadPool(), 10);
        var server = AsynchronousServerSocketChannel.open(group);
        //通过setOption配置Socket
        server.setOption(StandardSocketOptions.SO_REUSEADDR, true);
        server.setOption(StandardSocketOptions.SO_RCVBUF, 16 * 1024);
        //绑定到指定的主机，端口
        server.bind(new InetSocketAddress("localhost", 8887));
        System.out.println("Listening on " + "localhost" + ":" + 8887);
        //输出provider
        System.out.println("Channel Provider : " + server.provider());
        //等待连接，并注册CompletionHandler处理内核完成后的操作。
        server.accept(null, new CompletionHandler<>() {
            final ByteBuffer buffer = ByteBuffer.allocate(1024);

            @Override
            public void completed(AsynchronousSocketChannel result, Object attachment) {
                System.out.println("已连接请输入....");
                buffer.clear();
                try {
                    //把socket中的数据读取到buffer中
                    result.read(buffer).get();
                    buffer.flip();
                    System.out.println("接收到的请求 " + new String(buffer.array()).trim() + " \nfrom " + result);
                    buffer.compact();
                    buffer.put(" 已接收到你的信息\n".getBytes());
                    //把收到的直接返回给客户端
                    buffer.flip();
                    result.write(buffer);
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        result.close();
                        server.accept(null, this);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void failed(Throwable exc, Object attachment) {
                System.out.print("Server failed...." + exc.getCause());
            }
        });
        try {
            Thread.sleep(Integer.MAX_VALUE);//因为AIO不会阻塞调用进程，因此必须在主进程阻塞，才能保持进程存活。
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
