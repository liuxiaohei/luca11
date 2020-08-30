package org.ld.task;

import org.ld.pojo.Job;
import org.ld.utils.StringUtil;
import org.ld.utils.ZLogger;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Component
@EnableScheduling
public class RefreshServiceTask {
    private final Logger logger = ZLogger.newInstance();
    @Autowired
    private DiscoveryClient discoveryClient;
    public static final Map<String, List<Job>> servicesMap = new ConcurrentHashMap<>();
    @Autowired
    private LoadBalancerClient loadBalancerClient;

    /**
     * 初始化服务地址
     */
    @PostConstruct
    public void initServices() {
        logger.info("初始化可用服务列表");
        List<String> services = discoveryClient.getServices();
        for (String serviceName : services) {
            List<Job> jobs = getServiceInstance(serviceName);
            if (StringUtil.isNotEmpty(jobs)) {
                servicesMap.put(serviceName, jobs);
            }
        }

    }

    /**
     * 获取服务详细信息
     * @param serviceName 服务名
     */
    public List<Job> getServiceInstance(String serviceName) {
        List<Job> jobs = new ArrayList<>();
        ServiceInstance choose = loadBalancerClient.choose(serviceName);
        if (choose == null) {
            return jobs;
        }
        Map<String, String> metadata = choose.getMetadata();
        if (metadata.get("grpcPort") != null) {
            Job job = new Job();
            job.setHost(choose.getHost());
            job.setPort(Integer.valueOf(metadata.get("grpcPort")));
            jobs.add(job);
        }
        return jobs;
    }

    @Scheduled(cron = "0 0/5 * * * * ")
    public void refreshServices() {
        logger.info("定时刷新服务列表");
        initServices();
    }
}
