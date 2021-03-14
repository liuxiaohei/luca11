package org.ld.beans;

import lombok.Getter;
import org.ld.exception.CodeStackException;

import java.io.*;

/**
 * 可以用于这样一个场景
 * 一个线程一直往 outputStream 写入
 * 另一个线程从 inputStream 读取上个线程读取的结果 一边读一边写同时进行
 * <p>
 * 好处
 * 1 规避了 只有 outputStream 写完才能返回 inputStream 的问题 这样读取更快
 * 2 边读边写 真正的流式读写 不会有内存积压
 * 坏处
 * 要注意拿到 outputStream 和 inputStream 之后不能在同一个线程内读写 会发生死锁
 * buffer满的时候需要等待消费 造成block 所以大小最好是自定义
 */
@Getter
public class CircularByteBuffer {

    OutputStream outputStream;

    InputStream inputStream;

    public CircularByteBuffer(int bufferSize) {
        outputStream = new PipedOutputStream();
        try {
            inputStream = new PipedInputStream((PipedOutputStream) outputStream, bufferSize);
        } catch (IOException e) {
            throw new CodeStackException(e);
        }
    }

    public CircularByteBuffer() {
        this(1024);
    }
}
