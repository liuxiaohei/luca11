package org.ld.examples.nio.socket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

public class AIOSocket {
    private AsynchronousServerSocketChannel server;

    public static void main(String[] args) throws IOException {
        new Thread() {
            @Override
            public void run() {
                try {
                    sleep(1000);
                    Socket socket = new Socket("localhost", 8887);
                    BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
                    BufferedReader resp = null;
                    String line = reader.readLine();
                    socket.getOutputStream().write(line.getBytes());
                    resp = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    System.out.println(" 接收到的响应 " + resp.readLine());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                System.exit(1);
            }
        }.start();

        AIOSocket aioServer = new AIOSocket();
        aioServer.init("localhost", 8887);
    }

    private void init(String host, int port) throws IOException {
        //ChannelGroup用来管理共享资源
        AsynchronousChannelGroup group = AsynchronousChannelGroup.withCachedThreadPool(Executors.newCachedThreadPool(), 10);
        server = AsynchronousServerSocketChannel.open(group);
        //通过setOption配置Socket
        server.setOption(StandardSocketOptions.SO_REUSEADDR, true);
        server.setOption(StandardSocketOptions.SO_RCVBUF, 16 * 1024);
        //绑定到指定的主机，端口
        server.bind(new InetSocketAddress(host, port));
        System.out.println("Listening on " + host + ":" + port);
        //输出provider
        System.out.println("Channel Provider : " + server.provider());
        //等待连接，并注册CompletionHandler处理内核完成后的操作。
        server.accept(null, new CompletionHandler<AsynchronousSocketChannel, Object>() {
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
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } finally {
                    try {
//                        //关闭处理完的socket，并重新调用accept等待新的连接
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

        //因为AIO不会阻塞调用进程，因此必须在主进程阻塞，才能保持进程存活。
        try {
            Thread.sleep(Integer.MAX_VALUE);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
