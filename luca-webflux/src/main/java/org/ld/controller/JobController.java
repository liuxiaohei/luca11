package org.ld.controller;

import org.ld.beans.JobQuery;
import org.ld.grpc.schedule.ScheduleJob;
import org.ld.service.JobService;
import org.ld.utils.NumberUtil;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Optional;


@RestController
public class JobController {

    @Resource
    private JobService jobService;

    @RequestMapping(value = "services", method = RequestMethod.GET)
    public Object queryServices() {
        return jobService.queryServices();
    }

    @RequestMapping(value = "job", method = RequestMethod.POST)
    public Object save(@RequestBody ScheduleJob jobBean) {
        jobService.validate(jobBean);
        return jobService.save(jobBean);
    }

    @RequestMapping(value = "job/{jobId}", method = RequestMethod.DELETE)
    public Object delete(@PathVariable("jobId") Integer jobId) {
        Optional.ofNullable(jobId)
                .filter(NumberUtil::isValidId)
                .orElseThrow(() -> new RuntimeException("error_job_jobId"));
        jobService.delete(jobId);
        return null;
    }

    @RequestMapping(value = "job", method = RequestMethod.PUT)
    public Object update(@RequestBody ScheduleJob jobBean) {
        Optional.ofNullable(jobBean.getId())
                .filter(NumberUtil::isValidId)
                .orElseThrow(() -> new RuntimeException("error_job_jobId"));
        jobService.update(jobBean);
        return null;
    }

    @RequestMapping(value = "job/{jobId}", method = RequestMethod.GET)
    public Object queryJob(@PathVariable("jobId") Integer jobId) {
        Optional.ofNullable(jobId)
                .filter(NumberUtil::isValidId)
                .orElseThrow(() -> new RuntimeException("error_job_jobId"));
        return jobService.queryJob(jobId);
    }

    @RequestMapping(value = "jobs", method = RequestMethod.POST)
    public Object queryJobList(@RequestBody JobQuery query) {
        return jobService.queryJobList(query);
    }

    @RequestMapping(value = "run/{jobId}", method = RequestMethod.GET)
    public Object runJob(@PathVariable("jobId") Integer jobId) {
        Optional.ofNullable(jobId)
                .filter(NumberUtil::isValidId)
                .orElseThrow(() -> new RuntimeException("error_job_jobId"));
        jobService.run(jobId);
        return null;
    }

    @RequestMapping(value = "pauseJob/{jobId}", method = RequestMethod.GET)
    public Object pauseJob(@PathVariable("jobId") Integer jobId) {
        Optional.ofNullable(jobId)
                .filter(NumberUtil::isValidId)
                .orElseThrow(() -> new RuntimeException("error_job_jobId"));
        jobService.pauseJob(jobId);
        return null;
    }

    @RequestMapping(value = "resumeJob/{jobId}", method = RequestMethod.GET)
    public Object resumeJob(@PathVariable("jobId") Integer jobId) {
        Optional.ofNullable(jobId)
                .filter(NumberUtil::isValidId)
                .orElseThrow(() -> new RuntimeException("error_job_jobId"));
        jobService.resumeJob(jobId);
        return null;
    }

}
