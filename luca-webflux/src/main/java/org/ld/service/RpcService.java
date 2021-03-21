package org.ld.service;

import lombok.extern.slf4j.Slf4j;

import org.ld.task.RefreshServiceTask;
import org.ld.schedule.ScheduleJob;
import org.ld.schedule.client.ScheduleClient;
import org.ld.utils.StringUtil;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Slf4j
@Service
public class RpcService {

    @Resource
    private RefreshServiceTask refreshServiceTask;
    @Resource
    JobService jobService;
    @Resource
    ScheduleClient scheduleClient;

    public void sendClient(ScheduleJob job) {
        var jobs = refreshServiceTask.getServiceInstance(job.getServiceName());
        String host = null;
        Integer port = null;
        if (StringUtil.isNotEmpty(jobs)) {
            RefreshServiceTask.servicesMap.put(job.getServiceName(), jobs);
            scheduleClient.send(job,jobs.get(0).getHost(),jobs.get(0).getPort());
        }

    }

}
