package org.ld.service;

import org.ld.beans.JobBean;
import org.ld.beans.JobQuery;
import org.ld.beans.PageData;
import org.ld.beans.ServiceBean;
import org.ld.enums.EnumDeleted;
import org.ld.mapper.JobMapper;
import org.ld.pojo.Job;
import org.ld.pojo.JobExample;
import org.ld.utils.JobUtils;
import org.ld.utils.StringUtil;
import org.quartz.Scheduler;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class JobService {
    @Resource
    private DiscoveryClient discoveryClient;
    @Resource
    private Scheduler scheduler;
    @Resource
    private JobMapper jobMapper;

    public PageData<ServiceBean> queryServices() {
        PageData<ServiceBean> pageData = new PageData<>();
        List<String> services = discoveryClient.getServices();
        List<ServiceBean> serviceList = new ArrayList<>();
        services.stream().distinct().forEach(e -> {
            ServiceBean serviceBean = new ServiceBean();
            serviceBean.serviceName = e;
            serviceList.add(serviceBean);
        });
        pageData.setList(serviceList);
        return pageData;
    }

    @Transactional
    public JobBean save(JobBean jobBean) {
        //名称是否重复
        JobExample jobExample = new JobExample();
        JobExample.Criteria criteria = jobExample.createCriteria();
        criteria.andNameEqualTo(jobBean.name);
        criteria.andDeletedEqualTo(EnumDeleted.NO.getValue());
        Optional.of(jobMapper.countByExample(jobExample)).filter(e -> e == 0).orElseThrow(() -> new
                RuntimeException("error_job_name"));
        //查询可用服务
        List<ServiceInstance> instanceList = discoveryClient.getInstances(jobBean.serviceName);
        for (ServiceInstance instance: instanceList) {
            Map<String, String> metadata = instance.getMetadata();
            if (metadata.get("grpcPort") != null) {
                jobBean.host = instance.getHost();
                jobBean.port = Integer.valueOf(metadata.get("grpcPort"));
                break;
            }
        }
        if (jobBean.host == null || jobBean.port == null) {
            throw new RuntimeException( "error_service_rpc_null");
        }
        Job job = couponJob(jobBean);
        int count = jobMapper.insertSelective(job);
        Optional.of(count).filter(e -> e > 0).orElseThrow(() -> new RuntimeException("error_job_save"));
        //创建任务执行器并执行任务
        JobUtils.createScheduleJob(scheduler,job);
        jobBean.id = job.getId();
        return jobBean;
    }

    private Job couponJob(JobBean jobBean) {
        Job job = new Job();
        job.setBeanName(jobBean.beanName);
        job.setServiceName(jobBean.serviceName);
        job.setName(jobBean.name);
        job.setMethodName(jobBean.methodName);
        job.setHost(jobBean.host);
        job.setPort(jobBean.port);
        job.setCronExpression(jobBean.cronExpression);
        return job;
    }

    @Transactional
    public void delete(Integer jobId) {
        Job job = getAndCheckJob(jobId);
        job.setDeleted(EnumDeleted.YES.getValue());
        int count = jobMapper.updateByPrimaryKeySelective(job);
        Optional.of(count)
                .filter(e -> e > 0)
                .orElseThrow(() -> new RuntimeException("error_job_delete"));
        //删除任务
        JobUtils.deleteScheduleJob(scheduler,jobId);
    }

    @Transactional
    public void update(JobBean jobBean) {
        Job job = getAndCheckJob(jobBean.id);
        checkAndSet(jobBean,job);
        //名称是否重复
        JobExample jobExample = new JobExample();
        JobExample.Criteria criteria = jobExample.createCriteria();
        criteria.andNameEqualTo(jobBean.name);
        criteria.andDeletedEqualTo(EnumDeleted.NO.getValue());
        criteria.andIdNotEqualTo(jobBean.id);
        Optional.of(jobMapper.countByExample(jobExample)).filter(e -> e == 0).orElseThrow(() -> new RuntimeException( "error_job_name"));
        //数据校验
        validate(jobBean);
        Job jobReq = couponJob(jobBean);
        jobReq.setId(job.getId());
        jobReq.setStatus(job.getStatus());
        int count = jobMapper.updateByPrimaryKeySelective(jobReq);
        Optional.of(count).filter(e -> e > 0).orElseThrow(() -> new RuntimeException( "error_job_update"));
        JobUtils.updateScheduleJob(scheduler,jobReq);
    }

    private void checkAndSet(JobBean jobBean, Job job) {
        jobBean.name = Optional.ofNullable(jobBean.name).orElse(job.getName());
        jobBean.beanName = Optional.ofNullable(jobBean.beanName).orElse(job.getBeanName());
        jobBean.methodName = Optional.ofNullable(jobBean.methodName).orElse(job.getMethodName());
        jobBean.cronExpression = Optional.ofNullable(jobBean.cronExpression).orElse(job.getCronExpression());
        jobBean.params = Optional.ofNullable(jobBean.params).orElse(job.getParams());
        jobBean.status = job.getStatus();
        jobBean.host = job.getHost();
        jobBean.port = job.getPort();
    }

    public JobBean queryJob(Integer jobId) {
        JobQuery query = new JobQuery();
        query.jobId = jobId;
        PageData<JobBean> data = queryJobList(query);
        if (StringUtil.isEmpty(data.getList())) {
            throw new RuntimeException();
        }
        return data.getList().get(0);
    }

    public PageData<JobBean> queryJobList(JobQuery query) {
        JobExample jobExample = new JobExample();
        JobExample.Criteria criteria = jobExample.createCriteria();
        criteria.andDeletedEqualTo(EnumDeleted.NO.getValue());
        if (StringUtil.isNotBlank(query.jobName)) {
            criteria.andNameLike("%" + query.jobName + "%");
        }
        if (query.jobId != null) {
            criteria.andIdEqualTo(query.jobId);
        }
        if (StringUtil.isNotBlank(query.serviceName)) {
            criteria.andServiceNameEqualTo(query.serviceName);
        }
        PageData<JobBean> pageData = new PageData<>();
        pageData.setCount(jobMapper.countByExample(jobExample));
        //query.setAll(Boolean.FALSE);
        jobExample.setOffset(query.getOffSet());
        jobExample.setLimit(query.getLimit());
        List<Job> jobs = jobMapper.selectByExample(jobExample);
        if (StringUtil.isEmpty(jobs)) {
            pageData.setList(new ArrayList<>());
            return pageData;
        }
        pageData.setList(convertJobs(jobs));
        return pageData;
    }

    /**
     * 类型转换
     * @param jobs 任务列表
     */
    private List<JobBean> convertJobs(List<Job> jobs) {
        List<JobBean> list = new ArrayList<>();
        jobs.forEach(e -> {
            JobBean jobBean = new JobBean();
            jobBean.id = e.getId();
            jobBean.port = e.getPort();
            jobBean.host = e.getHost();
            jobBean.beanName = e.getBeanName();
            jobBean.methodName = e.getMethodName();
            jobBean.serviceName = e.getServiceName();
            jobBean.cronExpression = e.getCronExpression();
            jobBean.params = e.getParams();
            jobBean.createTime = e.getCreateTime();
            jobBean.status = e.getStatus();
            list.add(jobBean);
        });
        return list;
    }

    /**
     * 立即执行任务,执行一次
     */
    public void run(Integer jobId) {
        Job job = getAndCHeckJob(jobId);
        JobUtils.run(scheduler,job);
    }

    /**
     *获取任务信息
     */
    private Job getAndCHeckJob(Integer jobId) {
        Job job = jobMapper.selectByPrimaryKey(jobId);
        Optional.ofNullable(job).orElseThrow(() -> new RuntimeException("error_job_not_exist"));
        Optional.of(job)
                .filter(e -> job.getDeleted().equals(EnumDeleted.NO.getValue()))
                .orElseThrow(() -> new RuntimeException("error_job_delete_status"));
        return job;
    }

    /**
     * 暂停任务
     * @param jobId 任务id
     */
    @Transactional
    public void pauseJob(Integer jobId) {
        Job job = getAndCHeckJob(jobId);
        job.setStatus(JobUtils.STATUS);
        int count = jobMapper.updateByPrimaryKeySelective(job);
        Optional.of(count).filter(e -> e > 0).orElseThrow(() -> new RuntimeException("error_job_update"));
        JobUtils.pauseJob(scheduler,jobId);
    }

    /**
     * 恢复任务
     * @param jobId 任务id
     */
    @Transactional
    public void resumeJob(Integer jobId) {
        Job job = getAndCHeckJob(jobId);
        job.setStatus(0);
        int count = jobMapper.updateByPrimaryKeySelective(job);
        Optional.of(count).filter(e -> e > 0).orElseThrow(() -> new RuntimeException("error_job_update"));
        JobUtils.resumeJob(scheduler,jobId);
    }

    /**
     * 数据校验
     */
    public void validate(JobBean jobBean) {
        Optional.ofNullable(jobBean.name)
                .filter(StringUtil::isNotBlank)
                .orElseThrow(() -> new RuntimeException("error_job_name_null"));
        Optional.ofNullable(jobBean.serviceName)
                .filter(StringUtil::isNotBlank)
                .orElseThrow(() -> new RuntimeException("error_service_name_null"));
        Optional.ofNullable(jobBean.beanName)
                .filter(StringUtil::isNotBlank)
                .orElseThrow(() -> new RuntimeException("error_bean_name_null"));
        Optional.ofNullable(jobBean.methodName)
                .filter(StringUtil::isNotBlank)
                .orElseThrow(() -> new RuntimeException("error_method_name_null"));
        Optional.ofNullable(jobBean.cronExpression)
                .filter(StringUtil::isNotBlank)
                .orElseThrow(() -> new RuntimeException("error_cron_expression_null"));

    }

    /**
     * 获取任务并检查
     * @param jobId 任务id
     * @return 任务实体
     */
    private Job getAndCheckJob(Integer jobId) {
        Job job = jobMapper.selectByPrimaryKey(jobId);
        Optional.ofNullable(job).orElseThrow(() -> new RuntimeException("error_job_not_exist"));
        Optional.of(job)
                .filter(e -> job.getDeleted().equals(EnumDeleted.NO.getValue()))
                .orElseThrow(() -> new RuntimeException("error_job_delete_status"));
        return job;
    }
}
