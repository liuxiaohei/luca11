package org.ld.config;

import lombok.Getter;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.ld.utils.Snowflake;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class BaseConfig {

    @Value("${dev.mode:127.0.0.1:2181}")
    private String zkConnectString;

    /**
     * https://blog.csdn.net/mnmlist/article/details/103449920
     * 分布式ID生成器 基于SnowflakeId算法生成id 基于 zk的临时有序节点来控制不同节点的设备id 来保证不重复 最多支持同一个集群内1024个节点同时生成不重复的id
     */
    @Bean
    public Snowflake snowflakeId() {
        return new Snowflake(() -> {
            //ZooKeeper客户端
            var client = CuratorFrameworkFactory.newClient(getZkConnectString(), new ExponentialBackoffRetry(1000, 3));
            client.start();
            var nodeName = "/IDMaker/ID-";
            String str = client.create()             // 创建一个 ZNode 顺序节点
                    .creatingParentsIfNeeded()
                    .withMode(CreateMode.EPHEMERAL_SEQUENTIAL)//避免zookeeper的顺序节点暴增，可以删除创建的顺序节点
                    .forPath(nodeName);
            if (null == str) {
                return 0L;
            }
            var index = str.lastIndexOf(nodeName);
            if (index >= 0) {
                index += nodeName.length();
                str = index <= str.length() ? str.substring(index) : "";
            }
            return Long.parseLong(str) % 1024;
        });
    }
}
