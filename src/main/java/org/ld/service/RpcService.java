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
import org.ld.utils.grpc.proto.GreeterGrpc;
import org.ld.utils.grpc.proto.GrpcReply;
import org.ld.utils.grpc.proto.GrpcRequest;
import org.quartz.Scheduler;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class RpcService {
    private ManagedChannel channel;

    @Autowired
    private Scheduler scheduler;
    @Autowired
    private RefreshServiceTask refreshServiceTask;

    private final Logger log = ZLogger.newInstance();

    /**
     * 客户端发起调用
     *
     * @param params 客户端发起请求
     */
    public void sendClient(String params) throws InterruptedException {
        Job job = JsonUtil.json2Obj(params, Job.class);
        Map<String, List<Job>> map = RefreshServiceTask.servicesMap;
        List<Job> jobs = refreshServiceTask.getServiceInstance(job.getServiceName());
        if (StringUtil.isNotEmpty(jobs)) {
            job.setHost(jobs.get(0).getHost());
            job.setPort(jobs.get(0).getPort());
            map.put(job.getServiceName(), jobs);
        }
        this.channel = ManagedChannelBuilder.forAddress(job.getHost(), job.getPort())
                .usePlaintext()
                .build();
        GreeterGrpc.GreeterBlockingStub blockingStub = GreeterGrpc.newBlockingStub(channel);
        GrpcRequest request = GrpcRequest.newBuilder().setParams(params).build();
        try {
            GrpcReply grpcReply = blockingStub.sendMessage(request);
            if ("UNKNOWN".equals(grpcReply.getMessage())) {
                log.error("任务执行失败:{}，任务id:{}", grpcReply.getMessage(), job.getId());
            }
        } catch (StatusRuntimeException e) {
            List<Job> jobServices = map.get(job.getServiceName());
            if (StringUtil.isNotEmpty(jobServices) && jobServices.size() > 1) {
                jobServices.remove(0);
                job.setHost(jobServices.get(0).getHost());
                job.setPort(jobServices.get(0).getPort());
                map.put(job.getServiceName(), jobServices);
                JobUtils.updateScheduleJob(scheduler, job);
            } else {
                jobServices = refreshServiceTask.getServiceInstance(job.getServiceName());
                if (StringUtil.isNotEmpty(jobServices)) {
                    job.setHost(jobServices.get(0).getHost());
                    job.setPort(jobServices.get(0).getPort());
                    map.put(job.getServiceName(), jobServices);
                    JobUtils.updateScheduleJob(scheduler, job);
                }
            }
            System.out.println(e.getMessage());

        } finally {
            shutdown();
        }
    }

    public void shutdown() throws InterruptedException {
        this.channel.shutdown().awaitTermination(500, TimeUnit.SECONDS);
    }

}
