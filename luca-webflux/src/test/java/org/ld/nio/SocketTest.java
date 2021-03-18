package org.ld.nio;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.ld.exception.CodeStackException;
import org.ld.utils.SleepUtil;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

/**
 * https://www.jianshu.com/p/b9f3f6a16911
 */
@Slf4j
public class SocketTest {

    // 客户端 发送请求 并打印结果
    public static void clientRequest(int port) {
        new Thread(() -> {
            try {
                Thread.sleep(2000);
//                var socket = new Socket("localhost", port);
//                var in = socket.getInputStream();
//                var b = new byte[1024];
//                int len;
//                while ((len = in.read(b)) != -1) {
//                    log.info(new String(b, 0, len));
//                }
//                第二种NIO请求方式
                var channel = SocketChannel.open(new InetSocketAddress("localhost", port));
                var buffer = ByteBuffer.allocate(1024);
                channel.read(buffer);
                buffer.flip();
                log.info(new String(buffer.array(), 0, buffer.limit()));

//                第三种 AIO请求方式
//                var socket = new Socket("localhost", 8887);
//                var reader = new BufferedReader(new InputStreamReader(System.in));
//                var line = reader.readLine();
//                socket.getOutputStream().write(line.getBytes());
//                var resp = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//                log.info(" 接收到的响应 " + resp.readLine());

            } catch (Exception e) {
                throw CodeStackException.of(e);
            }
        }).start();
    }

    /**
     * Bio Test
     */
    @Test
    @SneakyThrows
    public void BSocket() {
        var port = 8987;
        clientRequest(port);
        // 服务端 返回请求
        final var socket = new ServerSocket(port);
        @Cleanup final var clientSocket = socket.accept();   // 监测到事件 阻塞1
        var out = clientSocket.getOutputStream();
        log.info("Accepted connection from " + clientSocket);
        out.write("Hi!\r\n".getBytes(StandardCharsets.UTF_8));// 数据传输  阻塞2
        out.flush();
        Thread.sleep(1000);
    }

    /**
     * NioTest
     */
    @Test
    public void NSocket() throws IOException {
        var port = 8988;
        clientRequest(port);
        // 服务端 返回请求
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
                        client.configureBlocking(false); // 触发内核启动非阻塞
                        client.register(selector, SelectionKey.OP_WRITE | SelectionKey.OP_READ, msg.duplicate());    //7
                        log.info("Accepted connection from " + client);
                    }
                    if (key.isWritable()) {
                        @Cleanup var client = (SocketChannel) key.channel();
                        var buffer = (ByteBuffer) key.attachment();
                        while (buffer.hasRemaining()) {
                            if (client.write(buffer) == 0) {
                                break;
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
        clientRequest(8887);
        //ChannelGroup用来管理共享资源
        var group = AsynchronousChannelGroup.withCachedThreadPool(Executors.newCachedThreadPool(), 10);
        var server = AsynchronousServerSocketChannel.open(group);
        //通过setOption配置Socket
        server.setOption(StandardSocketOptions.SO_REUSEADDR, true);
        server.setOption(StandardSocketOptions.SO_RCVBUF, 16 * 1024);
        //绑定到指定的主机，端口
        server.bind(new InetSocketAddress("localhost", 8887));
        log.info("Listening on " + "localhost" + ":" + 8887);
        //输出provider
        log.info("Channel Provider : " + server.provider());
        //等待连接，并注册CompletionHandler处理内核完成后的操作。
        server.accept(null, new CompletionHandler<>() {
            final ByteBuffer buffer = ByteBuffer.allocate(1024);

            @Override
            public void completed(AsynchronousSocketChannel result, Object attachment) {
                log.info("已连接请输入....");
                buffer.clear();
                try {
                    //把socket中的数据读取到buffer中
                    result.read(buffer).get();
                    buffer.flip();
                    log.info("接收到的请求 " + new String(buffer.array()).trim() + " \nfrom " + result);
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

    /**
     * Netty Test
     */
    @Test
    public void netty() throws InterruptedException {
        var port = 8987;
        clientRequest(port);
        // 服务端 返回请求
        var group = new NioEventLoopGroup();
        var c = new ServerBootstrap()
                .group(group)                                                                         //2
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<io.netty.channel.socket.SocketChannel>() {       //3
                    @Override
                    public void initChannel(io.netty.channel.socket.SocketChannel ch) {
                        ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {                    //4
                            @Override
                            public void channelActive(ChannelHandlerContext ctx) {
                                final var buf = Unpooled.unreleasableBuffer(Unpooled.copiedBuffer("Hi!\r\n", StandardCharsets.UTF_8));
                                ctx.writeAndFlush(buf.duplicate()).addListener(ChannelFutureListener.CLOSE);//5
                            }
                        });
                    }
                })
                .bind(port)
                .channel();

        CompletableFuture.runAsync(() -> {
            SleepUtil.sleep(3000);
            c.close();
        }); // 模拟链路关闭

        c.closeFuture() // 这个函数会阻塞住主线程 等待channel 关闭
                .sync()
                .addListener((ChannelFutureListener) future -> {
                    group.shutdownGracefully();        //7
                    log.info(future.channel().toString() + " 链路关闭");
                })
                .sync();//6
    }

}
