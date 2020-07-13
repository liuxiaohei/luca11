package org.ld.engine;

import org.ld.utils.JsonUtil;
import org.quartz.*;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;

@Component
public class Descriptor {

    @Resource
    private Scheduler scheduler;

    public void submit(Map<String, Runnable> params) throws SchedulerException {
        Trigger trigger = TriggerBuilder.newTrigger().withSchedule(SimpleScheduleBuilder.simpleSchedule()).startNow().build();
        JobDataMap jobDataMap = new JobDataMap(params);
        JobDetail jobDetail = JobBuilder.newJob(BaseQuartzJobBean.class)
                .withIdentity("aaa", JsonUtil.getShortUuid())
                .withDescription("[Nothing]")
                .setJobData(jobDataMap)
                .storeDurably()
                .build();
        scheduler.scheduleJob(jobDetail, trigger);
    }
}
