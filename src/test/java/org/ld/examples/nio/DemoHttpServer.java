package org.ld.examples.nio;

import org.junit.Test;
import org.ld.utils.StringUtil;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.concurrent.ForkJoinPool;

public class DemoHttpServer {

    /**
     * 客户端
     */
    @Test
    public void client() throws IOException {
        ForkJoinPool.commonPool().execute(() -> {
            try {
                Thread.sleep(1000);
                // 1、获取通道（channel）
                SocketChannel socketChannel = SocketChannel.open(new InetSocketAddress("127.0.0.1", 9898));
                // 2、切换成非阻塞模式
                socketChannel.configureBlocking(false);
                Selector selector = Selector.open();
                socketChannel.register(selector, SelectionKey.OP_READ);
                new Thread(() -> {
                    try {
                        while (selector.select() > -1) {
                            //遍历每个有可用IO操作Channel对应的SelectionKey
                            for (SelectionKey selectionKey : selector.selectedKeys()) {
                                //如果该SelectionKey对应的Channel中有可读的数据
                                if (selectionKey.isReadable()) {
                                    //使用NIO读取Channel中的数据
                                    SocketChannel sc = (SocketChannel) selectionKey.channel();//获取通道信息
                                    ByteBuffer buffer = ByteBuffer.allocate(1024);//分配缓冲区大小
                                    sc.read(buffer);//读取通道里面的数据
                                    buffer.flip();//调用此方法为一系列通道写入或获取操作做好准备
                                    // 将字节转化为为UTF-8的字符串
                                    String receivedString = StandardCharsets.UTF_8.newDecoder().decode(buffer).toString();
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
                }).start();

                // 3、分配指定大小的缓冲区
                ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
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
                socketChannel.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        server();
    }

    public void server() throws IOException {
        // 1、获取通道
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        // 2.设置为非阻塞
        serverSocketChannel.configureBlocking(false);
        // 3、绑定连接
        serverSocketChannel.bind(new InetSocketAddress(9898));

        // 4、获取Selector选择器
        Selector selector = Selector.open();

        // 5、将通道注册到选择器上,并制定监听事件
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        // 6、采用轮询的方式获取选择器上“准备就绪”的任务
        while (selector.select() > 0) {
            // 7、获取当前选择器中所有注册的选择键（“已经准备就绪的事件”）
            Iterator<SelectionKey> selectedKeys = selector.selectedKeys().iterator();
            while (selectedKeys.hasNext()) {
                // 8、获取“准备就绪”的时间
                SelectionKey selectedKey = selectedKeys.next();
                // 9、判断key是具体的什么事件
                if (selectedKey.isAcceptable()) {
                    // 10、若接受的事件是“接收就绪”事件,就获取客户端连接
                    SocketChannel socketChannel = serverSocketChannel.accept();
                    // 11、切换为非阻塞模式
                    socketChannel.configureBlocking(false);
                    // 12、将该通道注册到selector选择器上
                    socketChannel.register(selector, SelectionKey.OP_READ);
                } else if (selectedKey.isReadable()) {
                    // 13、获取该选择器上的“读就绪”状态的通道
                    SocketChannel socketChannel = (SocketChannel) selectedKey.channel();

                    ByteBuffer byteBuffer = ByteBuffer.allocate(1024 * 3);
                    // 14、将缓冲区清空以备下次读取
                    byteBuffer.clear();

                    // 读取服务器发送来的数据到缓冲区中
                    int readCount;
                    readCount = socketChannel.read(byteBuffer);

                    // 如果没有数据 则关闭
                    if (readCount == -1) {
                        Socket socket = socketChannel.socket();
                        SocketAddress remoteAddr = socket.getRemoteSocketAddress();
                        System.out.println("Connection closed by client: " + remoteAddr);
                        socketChannel.close();
                        selectedKey.cancel();
                        return;
                    }

                    // 有数据 则处理
                    String receiveText = new String(byteBuffer.array(), 0, readCount);
                    request(socketChannel, receiveText);
                    selectedKey.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);

                }
                selectedKeys.remove();
            }
        }

        // 7、关闭连接
        serverSocketChannel.close();
    }

    private void request(SocketChannel socketChannel, String req) throws IOException {
        if (StringUtil.isNotEmpty(req))
            System.out.println(System.currentTimeMillis() + " 接收到客户端的请求 " + req);
        ByteBuffer buffer = ByteBuffer.allocate(50);
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
