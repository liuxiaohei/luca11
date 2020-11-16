package org.ld;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalNotification;
import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import org.junit.jupiter.api.Test;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

//https://www.cnblogs.com/leeego-123/p/11393258.html
//https://blog.csdn.net/qb170217/article/details/81484139
public class CacheDemoTest {
    private static Cache<String, String> cache = CacheBuilder.newBuilder()
            .expireAfterWrite(30, TimeUnit.MINUTES) // 缓存时间
            .initialCapacity(10) //  缓存初始大小 10
            .concurrencyLevel(5) //  设置并发数为5，即同一时间最多只能有5个线程往cache执行写入操作
            .maximumSize(100)    //  缓存最大大小 100
            .removalListener((RemovalNotification<String, String> notification) -> {
                                 //  缓存到期被移除时做点什么
            })
            .build();

    @Test
    public void demo() throws Exception {
        System.out.println(cache.get("aaa",this::getbbb));
        System.out.println(cache.get("aaa",this::getbbb));
        cache.put("aaa",this.getccc());
        System.out.println(cache.get("aaa",this::getbbb));
        System.out.println(cache.get("aaa",this::getbbb));
    }

    private String getbbb() {
        return "bbb";
    }

    private String getccc() {
        return "ccc";
    }

    /**
     * 布隆过滤器的使用
     */
    @Test
    public void demo3() {
        // 参数1:字符编码集, 参数2:加入的key的数量, 参数3: 预期的误判率
        var boomFilter = BloomFilter.create(Funnels.stringFunnel(Charset.forName("utf-8")),1000000,0.0003);
        for (int i = 0; i < 1000000; i++) {
            // 加入key
            boomFilter.put(i+"abc");
        }
        int count =0;
        for (int i = 0; i < 2000000; i++) {
            // 判断是否存在
            if (boomFilter.mightContain(i+"abc")) {
                count ++;
            }
        }
        System.out.println("count :  "+count);
    }
//    https://blog.csdn.net/dnc8371/article/details/106814820
    /**
     * 如果您想暂停不到一毫秒，则需要忙于等待
     * System.nanoSecond（）大约需要40ns
     * Thread.sleep（1）的准确率只有75％
     * 忙于等待超过10us以上的时间几乎是100％准确的
     * 繁忙的等待将占用CPU
     */
    @Test
    public void  sleep() {
        long[] samples = new long[100_000];
        int pauseInMillis = 1;
        for (int i = 0; i < samples.length; i++) {
            long firstTime = System.nanoTime();
//            LockSupport.parkNanos(pauseInMillis); // 实现的效果当 pauseInMillis = 1时 平均时长差不多10000纳秒左右
            long timeForNano = System.nanoTime() - firstTime;
            samples[i] = timeForNano;
        }

        System.out.printf("Time for LockSupport.parkNanos() %.0f\n", Arrays.stream(samples).average().getAsDouble());
    }


}
