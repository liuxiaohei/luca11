package org.ld.service;

import lombok.SneakyThrows;
import org.ld.beans.JobQuery;
import org.ld.beans.PageData;
import org.ld.engine.JobRunnable;
import org.ld.exception.CodeStackException;
import org.ld.mapper.JobMapper;
import org.ld.pojo.JobExample;
import org.ld.schedule.ScheduleJob;
import org.ld.utils.JsonUtil;
import org.ld.utils.StringUtil;
import org.quartz.*;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class JobService {

    @Resource
    private DiscoveryClient discoveryClient;
    @Resource
    private Scheduler scheduler;
    @Resource
    private JobMapper jobMapper;

    public static final String JOB_PARAM_KEY = "JOB_PARAM_KEY";

    public PageData<JobQuery> queryServices() {
        PageData<JobQuery> pageData = new PageData<>();
        pageData.setList(discoveryClient.getServices().stream().distinct()
                .map(e -> {
                    JobQuery serviceBean = new JobQuery();
                    serviceBean.serviceName = e;
                    return serviceBean;
                }).collect(Collectors.toList()));
        return pageData;
    }

    @Transactional
    public ScheduleJob save(ScheduleJob jobBean) throws SchedulerException {
        JobExample jobExample = new JobExample();
        JobExample.Criteria criteria = jobExample.createCriteria();
        criteria.andNameEqualTo(jobBean.getName());
        criteria.andDeletedEqualTo(0);
        Optional.of(jobMapper.countByExample(jobExample)).filter(e -> e == 0).orElseThrow(() -> new RuntimeException("error_job_name"));
        List<ServiceInstance> instanceList = discoveryClient.getInstances(jobBean.getServiceName());
        for (ServiceInstance instance : instanceList) {
            Map<String, String> metadata = instance.getMetadata();
            if (metadata.get("grpcPort") != null) {
                jobBean.setHost(instance.getHost());
                jobBean.setPort(Integer.valueOf(metadata.get("grpcPort")));
                break;
            }
        }
        if (jobBean.getHost() == null || jobBean.getPort() == null) {
            throw new RuntimeException("error_service_rpc_null");
        }
        int count = jobMapper.insertSelective(jobBean);
        Optional.of(count).filter(e -> e > 0).orElseThrow(() -> new RuntimeException("error_job_save"));
        JobDetail jobDetail = JobBuilder.newJob(JobRunnable.class)
                .withIdentity(getJobKey(jobBean.getId())).build();
        CronScheduleBuilder scheduleBuilder = CronScheduleBuilder
                .cronSchedule(jobBean.getCronExpression())
                .withMisfireHandlingInstructionDoNothing();
        CronTrigger trigger = TriggerBuilder.newTrigger().withIdentity(getTriggerKey(jobBean.getId()))
                .withSchedule(scheduleBuilder).build();
        jobDetail.getJobDataMap().put(JOB_PARAM_KEY, jobBean);
        scheduler.scheduleJob(jobDetail, trigger);
        return jobBean;
    }

    @Transactional
    public void delete(Integer jobId) throws SchedulerException {
        ScheduleJob job = getAndCheckJob(jobId);
        job.setDeleted(1);
        int count = jobMapper.updateByPrimaryKeySelective(job);
        Optional.of(count).filter(e -> e > 0).orElseThrow(() -> new RuntimeException("error_job_delete"));
        scheduler.deleteJob(getJobKey(jobId));
    }

    @Transactional
    @SneakyThrows
    public void update(ScheduleJob jobBean) {
        ScheduleJob job = getAndCheckJob(jobBean.getId());
        checkAndSet(jobBean, job);
        JobExample jobExample = new JobExample();
        JobExample.Criteria criteria = jobExample.createCriteria();
        criteria.andNameEqualTo(jobBean.getName());
        criteria.andDeletedEqualTo(0);
        criteria.andIdNotEqualTo(jobBean.getId());
        Optional.of(jobMapper.countByExample(jobExample)).filter(e -> e == 0).orElseThrow(() -> new RuntimeException("error_job_name"));
        validate(jobBean);
        int count = jobMapper.updateByPrimaryKeySelective(jobBean);
        Optional.of(count).filter(e -> e > 0).orElseThrow(() -> new RuntimeException("error_job_update"));
        updateScheduleJob(scheduler, jobBean);
    }

    private void checkAndSet(ScheduleJob jobBean, ScheduleJob job) {
        jobBean.setName(Optional.ofNullable(jobBean.getName()).orElse(job.getName()));
        jobBean.setBeanName(Optional.ofNullable(jobBean.getBeanName()).orElse(job.getBeanName()));
        jobBean.setMethodName(Optional.ofNullable(jobBean.getMethodName()).orElse(job.getMethodName()));
        jobBean.setCronExpression(Optional.ofNullable(jobBean.getCronExpression()).orElse(job.getCronExpression()));
        jobBean.setParams(Optional.ofNullable(jobBean.getParams()).orElse(job.getParams()));
        jobBean.setStatus(job.getStatus());
        jobBean.setHost(job.getHost());
        jobBean.setPort(job.getPort());
    }

    public ScheduleJob queryJob(Integer jobId) {
        JobQuery query = new JobQuery();
        query.jobId = jobId;
        PageData<ScheduleJob> data = queryJobList(query);
        if (StringUtil.isEmpty(data.getList())) {
            throw new RuntimeException();
        }
        return data.getList().get(0);
    }

    public PageData<ScheduleJob> queryJobList(JobQuery query) {
        JobExample jobExample = new JobExample();
        JobExample.Criteria criteria = jobExample.createCriteria();
        criteria.andDeletedEqualTo(0);
        if (StringUtil.isNotBlank(query.jobName)) {
            criteria.andNameLike("%" + query.jobName + "%");
        }
        if (query.jobId != null) {
            criteria.andIdEqualTo(query.jobId);
        }
        if (StringUtil.isNotBlank(query.serviceName)) {
            criteria.andServiceNameEqualTo(query.serviceName);
        }
        PageData<ScheduleJob> pageData = new PageData<>();
        pageData.setCount(jobMapper.countByExample(jobExample));
        jobExample.setOffset(query.getOffSet());
        jobExample.setLimit(query.getLimit());
        List<ScheduleJob> jobs = jobMapper.selectByExample(jobExample);
        if (StringUtil.isEmpty(jobs)) {
            pageData.setList(new ArrayList<>());
            return pageData;
        }
        pageData.setList(convertJobs(jobs));
        return pageData;
    }

    private List<ScheduleJob> convertJobs(List<ScheduleJob> jobs) {
        return jobs.stream().map(e -> JsonUtil.copyObj(e, ScheduleJob.class)).collect(Collectors.toList());
    }

    public void run(Integer jobId) throws SchedulerException {
        ScheduleJob job = jobMapper.selectByPrimaryKey(jobId);
        JobDataMap dataMap = new JobDataMap();
        dataMap.put(JOB_PARAM_KEY, job);
        scheduler.triggerJob(getJobKey(job.getId()), dataMap);
    }

    @Transactional
    public void pauseJob(Integer jobId) throws SchedulerException {
        ScheduleJob job = jobMapper.selectByPrimaryKey(jobId);
        job.setStatus(STATUS);
        int count = jobMapper.updateByPrimaryKeySelective(job);
        Optional.of(count).filter(e -> e > 0).orElseThrow(() -> new RuntimeException("error_job_update"));
        scheduler.pauseJob(getJobKey(jobId));
    }

    @Transactional
    public void resumeJob(Integer jobId) throws SchedulerException {
        ScheduleJob job = jobMapper.selectByPrimaryKey(jobId);
        job.setStatus(0);
        jobMapper.updateByPrimaryKeySelective(job);
        scheduler.resumeJob(getJobKey(jobId));
    }

    public void validate(ScheduleJob jobBean) {
        Optional.ofNullable(jobBean.getBeanName())
                .filter(StringUtil::isNotBlank)
                .orElseThrow(() -> new RuntimeException("error_job_name_null"));
        Optional.ofNullable(jobBean.getServiceName())
                .filter(StringUtil::isNotBlank)
                .orElseThrow(() -> new RuntimeException("error_service_name_null"));
        Optional.ofNullable(jobBean.getBeanName())
                .filter(StringUtil::isNotBlank)
                .orElseThrow(() -> new RuntimeException("error_bean_name_null"));
        Optional.ofNullable(jobBean.getMethodName())
                .filter(StringUtil::isNotBlank)
                .orElseThrow(() -> new RuntimeException("error_method_name_null"));
        Optional.ofNullable(jobBean.getCronExpression())
                .filter(StringUtil::isNotBlank)
                .orElseThrow(() -> new RuntimeException("error_cron_expression_null"));

    }

    private ScheduleJob getAndCheckJob(Integer jobId) {
        ScheduleJob job = jobMapper.selectByPrimaryKey(jobId);
        Optional.of(job)
                .filter(e -> job.getDeleted().equals(0))
                .orElseThrow(() -> new RuntimeException("error_job_delete_status"));
        return job;
    }

    @SneakyThrows
    public static void updateScheduleJob(Scheduler scheduler, ScheduleJob scheduleJob) {
        TriggerKey triggerKey = getTriggerKey(scheduleJob.getId());
        CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(scheduleJob.getCronExpression())
                .withMisfireHandlingInstructionDoNothing();
        CronTrigger trigger = getCronTrigger(scheduler, scheduleJob.getId());
        trigger = trigger.getTriggerBuilder().withIdentity(triggerKey).withSchedule(scheduleBuilder).build();
        trigger.getJobDataMap().put(JOB_PARAM_KEY, scheduleJob);
        scheduler.rescheduleJob(triggerKey, trigger);
        if (scheduleJob.getStatus().equals(STATUS)) {
            scheduler.pauseJob(getJobKey(scheduleJob.getId()));
        }

    }

    private final static String JOB_NAME = "TASK_";

    public final static Integer STATUS = 1;

    public static TriggerKey getTriggerKey(Integer jobId) {
        return TriggerKey.triggerKey(JOB_NAME + jobId);
    }

    public static JobKey getJobKey(Integer jobId) {
        return JobKey.jobKey(JOB_NAME + jobId);
    }

    public static CronTrigger getCronTrigger(Scheduler scheduler, Integer jobId) {
        try {
            return (CronTrigger) scheduler.getTrigger(getTriggerKey(jobId));
        } catch (SchedulerException e) {
            throw CodeStackException.of(e);
        }
    }
}
