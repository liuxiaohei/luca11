package org.ld.nio;

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
                                    System.out.println(System.currentTimeMillis() + " 接收到来自服务器" + sc.socket().getRemoteSocketAddress() + "的信息:" + receivedString);
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
                                System.out.println("Connection closed by client: " + remoteAddr);
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
            System.out.println(System.currentTimeMillis() + " 接收到客户端的请求 " + req);
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
}
