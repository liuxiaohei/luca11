package org.ld.utils;

import org.ld.engine.RunnableQuartzJob;
import org.ld.engine.ScheduleJob;
import org.ld.exception.CodeStackException;
import org.quartz.*;

public class JobUtils {
    //key前缀
    private final static String JOB_NAME = "TASK_";
    //暂停任务
    public final static Integer STATUS = 1;
    /**
     * 任务调度参数key
     */
    public static final String JOB_PARAM_KEY = "JOB_PARAM_KEY";

    /**
     * 获取触发器key
     */
    public static TriggerKey getTriggerKey(Integer jobId) {
        return TriggerKey.triggerKey(JOB_NAME + jobId);
    }

    /**
     * 获取jobKey
     */
    public static JobKey getJobKey(Object jobId) {
        return JobKey.jobKey(JOB_NAME + jobId);
    }

    /**
     * 获取表达式触发器
     */
    public static CronTrigger getCronTrigger(Scheduler scheduler, Integer jobId) {
        try {
            return (CronTrigger) scheduler.getTrigger(getTriggerKey(jobId));
        } catch (SchedulerException e) {
            throw new CodeStackException(e);
        }
    }

    /**
     * 创建定时任务
     */
    public static void createScheduleJob(Scheduler scheduler, ScheduleJob scheduleJob) {
        try {
            JobDetail jobDetail = JobBuilder.newJob(RunnableQuartzJob.class) // todo
                    .withIdentity(getJobKey(scheduleJob.getId()))
                    .build();
            //表达式调度构建器
            CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(scheduleJob.getCronExpression())
                    .withMisfireHandlingInstructionDoNothing();
            //按新的cronExpression表达式构建一个新的trigger
            CronTrigger trigger = TriggerBuilder.newTrigger().withIdentity(getTriggerKey(scheduleJob.getId()))
                    .withSchedule(scheduleBuilder).build();
            //放入参数，运行时的方法可以获取
            jobDetail.getJobDataMap().put(JOB_PARAM_KEY, scheduleJob);
            scheduler.scheduleJob(jobDetail, trigger);
        } catch (SchedulerException e) {
            throw new CodeStackException(e);
        }
    }

    /**
     * 更新定时任务
     */
    public static void updateScheduleJob(Scheduler scheduler, ScheduleJob scheduleJob) {
        try {
            TriggerKey triggerKey = getTriggerKey(scheduleJob.getId());
            //表达式调度构建器
            CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(scheduleJob.getCronExpression())
                    .withMisfireHandlingInstructionDoNothing();
            CronTrigger trigger = getCronTrigger(scheduler, scheduleJob.getId());
            //按新的cronExpression表达式重新构建trigger
            trigger = trigger.getTriggerBuilder().withIdentity(triggerKey).withSchedule(scheduleBuilder).build();
            //参数
            trigger.getJobDataMap().put(JOB_PARAM_KEY, scheduleJob);
            scheduler.rescheduleJob(triggerKey, trigger);
            //暂停任务
            if (equals(scheduleJob.getStatus(),STATUS)) {
                pauseJob(scheduler, scheduleJob.getId());
            }
        } catch (SchedulerException e) {
            throw new CodeStackException(e);
        }
    }

    public static boolean equals(Integer v1, Integer v2) {
        return !(v1 == null || v2 == null) && v1.equals(v2);
    }


    /**
     * 立即执行任务,执行一次
     */
    public static void run(Scheduler scheduler, ScheduleJob job) {
        try {
            //参数
            JobDataMap dataMap = new JobDataMap();
            dataMap.put(JOB_PARAM_KEY, job);
            scheduler.triggerJob(getJobKey(job.getId()), dataMap);
        } catch (SchedulerException e) {
            throw new CodeStackException(e);
        }
    }

    /**
     * 暂停任务
     */
    public static void pauseJob(Scheduler scheduler, Integer jobId) {
        try {
            scheduler.pauseJob(getJobKey(jobId));
        } catch (SchedulerException e) {
            throw new CodeStackException(e);
        }
    }

    /**
     * 恢复任务
     */
    public static void resumeJob(Scheduler scheduler, Integer jobId) {
        try {
            scheduler.resumeJob(getJobKey(jobId));
        } catch (SchedulerException e) {
            throw new CodeStackException(e);
        }
    }

    /**
     * 删除定时任务
     */
    public static void deleteScheduleJob(Scheduler scheduler, Integer jobId) {
        try {
            scheduler.deleteJob(getJobKey(jobId));
        } catch (SchedulerException e) {
            throw new CodeStackException(e);
        }
    }
}
