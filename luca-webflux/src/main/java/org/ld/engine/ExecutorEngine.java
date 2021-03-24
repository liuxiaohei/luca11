package org.ld.engine;

import lombok.SneakyThrows;
import org.ld.uc.UCRunnable;
import org.ld.utils.Snowflake;
import org.quartz.*;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
public class ExecutorEngine {

    @Resource
    private Scheduler scheduler;

    @Resource
    private Snowflake snowflakeId;

    /**
     * runnable 中的任务 会立刻被调度执行
     */
    // todo misfire 处理
    public void runAsync(UCRunnable runnable) {
        runAsync(runnable, TriggerBuilder.newTrigger()
                .withSchedule(SimpleScheduleBuilder.simpleSchedule())
                .startNow()
                .build());
    }

    /**
     * runnable 中的任务 会按照 Trigger 的要求执行
     */
    @SneakyThrows
    public void runAsync(UCRunnable runnable, Trigger trigger) {
        final Map<String, UCRunnable> params = new HashMap<>();
        params.put("Runnable", runnable);
        final JobDataMap jobDataMap = new JobDataMap(params);
        JobDetail jobDetail = JobBuilder.newJob(RunnableQuartzJob.class)
                .withIdentity(snowflakeId.get().toString(), LocalDateTime.now().toString())
                .withDescription("[Nothing]")
                .setJobData(jobDataMap)
                .storeDurably()
                .build();
        scheduler.scheduleJob(jobDetail, trigger);
    }
}
