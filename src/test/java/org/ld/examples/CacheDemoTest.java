package org.ld.examples;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalNotification;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

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


}
