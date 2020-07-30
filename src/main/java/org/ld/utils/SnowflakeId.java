package org.ld.utils;

/**
 * https://www.cnblogs.com/relucent/p/4955340.html
 * Twitter_Snowflake
 * SnowFlake的结构如下(每部分用-分开):
 * 0 - 0000000000 0000000000 0000000000 0000000000 0 - 00000 - 00000 - 000000000000
 * 1位标识，由于long基本类型在Java中是带符号的，最高位是符号位，正数是0，负数是1，所以id一般是正数，最高位是0
 * 41位时间截(毫秒级)，注意，41位时间截不是存储当前时间的时间截，而是存储时间截的差值（当前时间截 - 开始时间截)
 * 得到的值），这里的的开始时间截，一般是我们的id生成器开始使用的时间，由我们程序来指定的（如下下面程序IdWorker类的startTime属性）。41位的时间截，可以使用69年，年T = (1L << 41) / (1000L * 60 * 60 * 24 * 365) = 69<br>
 * 10位的数据机器位，可以部署在1024个节点，包括5位datacenterId和5位workerId
 * 12位序列，毫秒内的计数，12位的计数顺序号支持每个节点每毫秒(同一机器，同一时间截)产生4096个ID序号
 * 加起来刚好64位，为一个Long型。
 * SnowFlake的优点是，整体上按照时间自增排序，并且整个分布式系统内不会产生ID碰撞(由数据中心ID和机器ID作区分)，并且效率较高，经测试，SnowFlake每秒能够产生26万ID左右。
 */
public class SnowflakeId {

    /**
     * 工作机器ID(0~31)
     */
    private final long workerId;

    /**
     * 数据中心ID(0~31)
     */
    private final long datacenterId;

    /**
     * 毫秒内序列(0~4095)
     */
    private long sequence = 0L;

    /**
     * 上次生成ID的时间截
     */
    private long lastTimestamp = -1L;

    private SnowflakeId(long workerId, long datacenterId) {
        this.workerId = workerId;
        this.datacenterId = datacenterId;
    }

    private static class Holder {
        private static final SnowflakeId idWorker = new SnowflakeId(0, 0);
    }

    /**
     * 最大可能会有20位
     */
    public static Long get() {
        return Holder.idWorker.nextId();
    }

    private static final long stepSize = 1024;

    /**
     * 基础序列号, 每发生一次时钟回拨, basicSequence += stepSize
     */
    private long basicSequence = 0L;

    /**
     * 获得下一个ID (该方法是线程安全的)
     */
    private synchronized long nextId() {
        long timestamp = timeGen();
        if (timestamp < lastTimestamp) {
            // https://blog.csdn.net/jiangqian6481/article/details/102888944 检测时钟回退并处理
            basicSequence += stepSize;
            if (basicSequence >= 4095) {
                basicSequence = 0;
            }
        }
        //如果是同一时间生成的，则进行毫秒内序列
        long sequenceBits = 12L;
        if (lastTimestamp == timestamp) {
            // 生成序列的掩码，这里为4095 (0b111111111111=0xfff=4095)
            long sequenceMask = ~(-1L << sequenceBits);
            sequence = (sequence + 1) & sequenceMask;
            if (sequence == 0) {
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            sequence = basicSequence;
        }
        lastTimestamp = timestamp;
        // 开始时间截
        long twepoch = 1596088093000L;
        long workerIdBits = 5L;
        long datacenterIdShift = sequenceBits + workerIdBits;
        long datacenterIdBits = 5L;
        long timestampLeftShift = sequenceBits + workerIdBits + datacenterIdBits;
        return ((timestamp - twepoch) << timestampLeftShift)
                | (datacenterId << datacenterIdShift)
                | (workerId << sequenceBits)
                | sequence;
    }

    /**
     * 阻塞到下一个毫秒，直到获得新的时间戳
     */
    private long tilNextMillis(long lastTimestamp) {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = timeGen();
        }
        return timestamp;
    }

    private long timeGen() {
        return System.currentTimeMillis();
    }
}
