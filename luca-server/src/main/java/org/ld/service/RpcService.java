package org.ld.service;

import io.grpc.StatusRuntimeException;
import org.ld.grpc.grpc.LucaGrpcClient;
import org.ld.grpc.schedule.ScheduleJob;
import org.ld.task.RefreshServiceTask;
import org.ld.utils.JobUtils;
import org.ld.utils.StringUtil;
import org.ld.utils.ZLogger;
import org.quartz.Scheduler;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class RpcService {

    @Resource
    private Scheduler scheduler;
    @Resource
    private RefreshServiceTask refreshServiceTask;

    private final Logger log = ZLogger.newInstance();

    public void sendClient(ScheduleJob job) {
        assert job != null;
        List<ScheduleJob> jobs = refreshServiceTask.getServiceInstance(job.getServiceName());
        if (StringUtil.isNotEmpty(jobs)) {
            job.setHost(jobs.get(0).getHost());
            job.setPort(jobs.get(0).getPort());
            RefreshServiceTask.servicesMap.put(job.getServiceName(), jobs);
        }
        try {
            LucaGrpcClient.sendMessage(job.getHost(), job.getPort(), job);
        } catch (StatusRuntimeException e) {
            List<ScheduleJob> jobServices = RefreshServiceTask.servicesMap.get(job.getServiceName());
            if (StringUtil.isNotEmpty(jobServices) && jobServices.size() > 1) {
                jobServices.remove(0);
                job.setHost(jobServices.get(0).getHost());
                job.setPort(jobServices.get(0).getPort());
                RefreshServiceTask.servicesMap.put(job.getServiceName(), jobServices);
                JobUtils.updateScheduleJob(scheduler, job);
            } else {
                jobServices = refreshServiceTask.getServiceInstance(job.getServiceName());
                if (StringUtil.isNotEmpty(jobServices)) {
                    job.setHost(jobServices.get(0).getHost());
                    job.setPort(jobServices.get(0).getPort());
                    RefreshServiceTask.servicesMap.put(job.getServiceName(), jobServices);
                    JobUtils.updateScheduleJob(scheduler, job);
                }
            }
            log.info(e.getMessage());
        }
    }

}
