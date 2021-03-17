package org.ld.utils;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.ld.config.BaseConfig;
import org.ld.exception.CodeStackException;

/**
 * https://blog.csdn.net/mnmlist/article/details/103449920
 * 分布式ID生成器 基于SnowflakeId算法生成id 基于 zk的临时有序节点来控制不同节点的设备id 来保证不重复 最多支持同一个集群内1024个节点同时生成不重复的id
 */
public class IDMaker {

    private static class Holder {
        private static final BaseConfig baseConfig = SpringBeanFactory.getBean(BaseConfig.class);
        private static final SnowflakeId idWorker = new SnowflakeId(IDMaker::makerId);
    }

    public static Long get() {
        return Holder.idWorker.get();
    }

    private static long makerId() {
        //ZooKeeper客户端
        CuratorFramework client = CuratorFrameworkFactory.newClient(Holder.baseConfig.getZkConnectString(), new ExponentialBackoffRetry(1000, 3));
        client.start();
        String nodeName = "/IDMaker/ID-";
        String str;
        try {
            str = client.create()             // 创建一个 ZNode 顺序节点
                    .creatingParentsIfNeeded()
                    .withMode(CreateMode.EPHEMERAL_SEQUENTIAL)//避免zookeeper的顺序节点暴增，可以删除创建的顺序节点
                    .forPath(nodeName);
        } catch (Exception e) {
            throw CodeStackException.of(e);
        }
        if (null == str) {
            return 0;
        }
        int index = str.lastIndexOf(nodeName);
        if (index >= 0) {
            index += nodeName.length();
            str = index <= str.length() ? str.substring(index) : "";
        }
        return Long.parseLong(str) % 1024;
    }
}
