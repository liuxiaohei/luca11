package org.ld.nio;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.ld.utils.StringUtil;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

/**
 * https://www.jianshu.com/p/ed0177a9b2e3
 */
@Slf4j
public class DemoHttpServer {

    /**
     * 客户端
     * https://www.cnblogs.com/secbook/archive/2012/08/24/2655189.html
     */
    @Test
    public void client() throws IOException {
        CompletableFuture.runAsync(() -> {
            try (var socketChannel = SocketChannel.open(new InetSocketAddress("127.0.0.1", 9898))) {
                Thread.sleep(1000);
                // 1、获取通道（channel）
                // 2、切换成非阻塞模式
                socketChannel.configureBlocking(false);
                var selector = Selector.open();
                socketChannel.register(selector, SelectionKey.OP_READ);
                CompletableFuture.runAsync(() -> {
                    try {
                        while (selector.select() > -1) {
                            //遍历每个有可用IO操作Channel对应的SelectionKey
                            for (SelectionKey selectionKey : selector.selectedKeys()) {
                                //如果该SelectionKey对应的Channel中有可读的数据
                                if (selectionKey.isReadable()) {
                                    //使用NIO读取Channel中的数据
                                    var sc = (SocketChannel) selectionKey.channel();//获取通道信息
                                    var buffer = ByteBuffer.allocate(1024);//分配缓冲区大小
                                    sc.read(buffer);//读取通道里面的数据
                                    buffer.flip();//调用此方法为一系列通道写入或获取操作做好准备
                                    // 将字节转化为为UTF-8的字符串
                                    var receivedString = StandardCharsets.UTF_8.newDecoder().decode(buffer).toString();
                                    // 控制台打印出来
                                    log.info(System.currentTimeMillis() + " 接收到来自服务器" + sc.socket().getRemoteSocketAddress() + "的信息:" + receivedString);
                                    // 为下一次读取作准备
                                    selectionKey.interestOps(SelectionKey.OP_READ);
                                }
                                //删除正在处理的SelectionKey
                                selector.selectedKeys().remove(selectionKey);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

                // 3、分配指定大小的缓冲区
                var byteBuffer = ByteBuffer.allocate(1024);
                byteBuffer.put("/user/get".getBytes());
                byteBuffer.flip();
                socketChannel.write(byteBuffer);
                Thread.sleep(1000);
                byteBuffer = ByteBuffer.allocate(1024);
                byteBuffer.clear();
                byteBuffer.put("/user/get1".getBytes());
                byteBuffer.flip();
                socketChannel.write(byteBuffer);
                Thread.sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        server();
    }

    public void server() throws IOException {
        // 1、获取通道
        try (var serverSocketChannel = ServerSocketChannel.open()) {
            // 2.设置为非阻塞
            serverSocketChannel.configureBlocking(false);
            // 3、绑定连接
            serverSocketChannel.bind(new InetSocketAddress(9898));
            // 4、获取Selector选择器
            var selector = Selector.open();
            // 5、将通道注册到选择器上,并制定监听事件
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            // 6、采用轮询的方式获取选择器上“准备就绪”的任务
            while (selector.select() > 0) {
                // 7、获取当前选择器中所有注册的选择键（“已经准备就绪的事件”）
                var selectedKeys = selector.selectedKeys().iterator();
                while (selectedKeys.hasNext()) {
                    // 8、获取“准备就绪”的时间
                    var selectedKey = selectedKeys.next();
                    // 9、判断key是具体的什么事件
                    if (selectedKey.isAcceptable()) {
                        // 10、若接受的事件是“接收就绪”事件,就获取客户端连接
                        var socketChannel = serverSocketChannel.accept();
                        // 11、切换为非阻塞模式
                        socketChannel.configureBlocking(false);
                        // 12、将该通道注册到selector选择器上
                        socketChannel.register(selector, SelectionKey.OP_READ);
                    } else if (selectedKey.isReadable()) {
                        // 13、获取该选择器上的“读就绪”状态的通道
                        var socketChannel = (SocketChannel) selectedKey.channel();
                        var byteBuffer = ByteBuffer.allocate(1024 * 3);
                        // 14、将缓冲区清空以备下次读取
                        byteBuffer.clear();
                        // 读取服务器发送来的数据到缓冲区中
                        int readCount;
                        readCount = socketChannel.read(byteBuffer);
                        // 如果没有数据 则关闭
                        if (readCount == -1) {
                            try (socketChannel) {
                                var socket = socketChannel.socket();
                                var remoteAddr = socket.getRemoteSocketAddress();
                                log.info("Connection closed by client: " + remoteAddr);
                            }
                            selectedKey.cancel();
                            return;
                        }
                        // 有数据 则处理
                        var receiveText = new String(byteBuffer.array(), 0, readCount);
                        request(socketChannel, receiveText);
                        selectedKey.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
                    }
                    selectedKeys.remove();
                }
            }
        }
    }

    private void request(SocketChannel socketChannel, String req) throws IOException {
        if (StringUtil.isNotEmpty(req))
            log.info(System.currentTimeMillis() + " 接收到客户端的请求 " + req);
        var buffer = ByteBuffer.allocate(50);
        if ("/user/get".equals(req)) {
            buffer.put("xhm".getBytes());
            buffer.flip();
            socketChannel.write(buffer);
        } else {
            buffer.put("Not Support".getBytes());
            buffer.flip();
            socketChannel.write(buffer);
        }
    }

    //netty
    // https://www.jianshu.com/p/fd815bd437cd
    @Test
    public void nettyServer() throws Exception {
        ServerBootstrap b = new ServerBootstrap();
        NioEventLoopGroup group = new NioEventLoopGroup();
        b.group(group)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<io.netty.channel.socket.SocketChannel>() {
                    @Override
                    public void initChannel(io.netty.channel.socket.SocketChannel ch) {
                        log.info("initChannel ch:" + ch);
                        ch.pipeline()
                                .addLast("decoder", new HttpRequestDecoder())   // 1 将字节流转换为实体对象Message
                                .addLast("encoder", new HttpResponseEncoder())  // 2 ​ Encoder最重要的实现类是MessageToByteEncoder<T>，这个类的作用就是将消息实体T从对象转换成byte，写入到ByteBuf，然后再丢给剩下的ChannelOutboundHandler传给客户端，流程图如下：
                                .addLast("aggregator", new HttpObjectAggregator(512 * 1024))    // 3
                                .addLast("handler", new SimpleChannelInboundHandler<FullHttpRequest>() {
                                    @Override
                                    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg) {
                                        log.info("class:" + msg.getClass().getName());
                                        DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                                                HttpResponseStatus.OK,
                                                Unpooled.wrappedBuffer("test".getBytes())); // 2
                                        HttpHeaders heads = response.headers();
                                        heads.add(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.TEXT_PLAIN + "; charset=UTF-8");
                                        heads.add(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes()); // 3
                                        heads.add(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
                                        ctx.write(response);
                                    }

                                    @Override
                                    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
                                        log.info("channelReadComplete");
                                        super.channelReadComplete(ctx);
                                        ctx.flush(); // 4
                                    }

                                    @Override
                                    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
                                        log.info("exceptionCaught");
                                        if (null != cause) cause.printStackTrace();
                                        if (null != ctx) ctx.close();
                                    }
                                });
                    }
                })
                .option(ChannelOption.SO_BACKLOG, 128) // determining the number of connections queued
                .childOption(ChannelOption.SO_KEEPALIVE, Boolean.TRUE);

        b.bind(9999).sync();
        Thread.sleep(100000);
    }


}
