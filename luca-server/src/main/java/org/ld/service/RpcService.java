package org.ld.service;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import org.ld.pojo.Job;
import org.ld.task.RefreshServiceTask;
import org.ld.utils.JobUtils;
import org.ld.utils.JsonUtil;
import org.ld.utils.StringUtil;
import org.ld.utils.ZLogger;
import org.ld.grpc.client.GreeterGrpc;
import org.ld.grpc.client.GrpcReply;
import org.ld.grpc.client.GrpcRequest;
import org.quartz.Scheduler;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class RpcService {

    @Resource
    private Scheduler scheduler;
    @Resource
    private RefreshServiceTask refreshServiceTask;

    private final Logger log = ZLogger.newInstance();

    public void sendClient(Job job) throws InterruptedException {
        assert job != null;
        List<Job> jobs = refreshServiceTask.getServiceInstance(job.getServiceName());
        if (StringUtil.isNotEmpty(jobs)) {
            job.setHost(jobs.get(0).getHost());
            job.setPort(jobs.get(0).getPort());
            RefreshServiceTask.servicesMap.put(job.getServiceName(), jobs);
        }
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress(job.getHost(), job.getPort())
                .usePlaintext()
                .build();
        GreeterGrpc.GreeterBlockingStub blockingStub = new GreeterGrpc.GreeterBlockingStub(channel);
        GrpcRequest request = new GrpcRequest(JsonUtil.obj2Json(job));
        try {
            GrpcReply grpcReply = blockingStub.sendMessage(request);
            if ("UNKNOWN".equals(grpcReply.getMessage())) {
                log.error("任务执行失败:{}，任务id:{}", grpcReply.getMessage(), job.getId());
            }
        } catch (StatusRuntimeException e) {
            List<Job> jobServices = RefreshServiceTask.servicesMap.get(job.getServiceName());
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
        } finally {
            channel.shutdown().awaitTermination(500, TimeUnit.SECONDS);
        }
    }

}
