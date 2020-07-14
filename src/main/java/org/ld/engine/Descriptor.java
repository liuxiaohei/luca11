package org.ld.engine;

import org.ld.uc.UCRunnable;
import org.ld.utils.JsonUtil;
import org.quartz.*;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@Component
public class Descriptor {

    @Resource
    private Scheduler scheduler;

    /**
     * runnable 中的任务 会立刻被调度执行
     */
    public void runAsync(UCRunnable runnable) throws SchedulerException {
        runAsync(runnable, TriggerBuilder.newTrigger()
                .withSchedule(SimpleScheduleBuilder.simpleSchedule())
                .startNow()
                .build());
    }

    /**
     * runnable 中的任务 会按照 Trigger 的要求执行
     */
    public void runAsync(UCRunnable runnable, Trigger trigger) throws SchedulerException {
        final Map<String, UCRunnable> params = new HashMap<>();
        params.put("Runnable", runnable);
        final JobDataMap jobDataMap = new JobDataMap(params);
        JobDetail jobDetail = JobBuilder.newJob(RunnableQuartzJob.class)
                .withIdentity(JsonUtil.getShortUuid(), "DefaultGroup")
                .withDescription("[Nothing]")
                .setJobData(jobDataMap)
                .storeDurably()
                .build();
        scheduler.scheduleJob(jobDetail, trigger);
    }
}
