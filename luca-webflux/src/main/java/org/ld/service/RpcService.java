package org.ld.service;

import lombok.extern.slf4j.Slf4j;
import org.ld.exception.CodeStackException;
import org.ld.schedule.ScheduleJob;
import org.ld.schedule.client.ScheduleClient;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Optional;

@Slf4j
@Service
public class RpcService {

    @Resource
    private LoadBalancerClient loadBalancerClient;
    @Resource
    private ScheduleClient scheduleClient;

    public Object sendClient(ScheduleJob job) {
        var e = Optional.ofNullable(loadBalancerClient.choose(job.getServiceName())).orElseThrow(() -> new CodeStackException("服务不存在"));
        return scheduleClient.send(job, e.getHost(), e.getPort());
    }
}
