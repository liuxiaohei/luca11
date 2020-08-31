package org.ld.utils;

import org.ld.engine.JobRunnable;
import org.ld.exception.CodeStackException;
import org.ld.pojo.Job;
import org.quartz.*;

@SuppressWarnings("unused")
public class JobUtils {

    private final static String JOB_NAME = "TASK_";

    public final static Integer STATUS = 1;

    public static final String JOB_PARAM_KEY = "JOB_PARAM_KEY";

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
            throw new CodeStackException(e);
        }
    }

    public static void createScheduleJob(Scheduler scheduler, org.ld.pojo.Job scheduleJob) {
        try {
            JobDetail jobDetail = JobBuilder.newJob(JobRunnable.class).withIdentity(getJobKey(scheduleJob.getId())
            ).build();
            CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(scheduleJob.getCronExpression())
                    .withMisfireHandlingInstructionDoNothing();
            CronTrigger trigger = TriggerBuilder.newTrigger().withIdentity(getTriggerKey(scheduleJob.getId()))
                    .withSchedule(scheduleBuilder).build();
            jobDetail.getJobDataMap().put(JOB_PARAM_KEY, scheduleJob);
            scheduler.scheduleJob(jobDetail, trigger);
        } catch (SchedulerException e) {
            throw new CodeStackException(e);
        }
    }

    public static void updateScheduleJob(Scheduler scheduler, org.ld.pojo.Job scheduleJob) {
        try {
            TriggerKey triggerKey = getTriggerKey(scheduleJob.getId());
            CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(scheduleJob.getCronExpression())
                    .withMisfireHandlingInstructionDoNothing();

            CronTrigger trigger = getCronTrigger(scheduler, scheduleJob.getId());
            trigger = trigger.getTriggerBuilder().withIdentity(triggerKey).withSchedule(scheduleBuilder).build();
            trigger.getJobDataMap().put(JOB_PARAM_KEY, scheduleJob);
            scheduler.rescheduleJob(triggerKey, trigger);
            if (scheduleJob.getStatus().equals(STATUS)) {
                pauseJob(scheduler, scheduleJob.getId());
            }
        } catch (SchedulerException e) {
            throw new CodeStackException(e);
        }
    }

    public static void run(Scheduler scheduler, Job job) {
        try {
            JobDataMap dataMap = new JobDataMap();
            dataMap.put(JOB_PARAM_KEY, job);
            scheduler.triggerJob(getJobKey(job.getId()), dataMap);
        } catch (SchedulerException e) {
            throw new CodeStackException(e);
        }
    }

    public static void pauseJob(Scheduler scheduler, Integer jobId) {
        try {
            scheduler.pauseJob(getJobKey(jobId));
        } catch (SchedulerException e) {
            throw new CodeStackException(e);
        }
    }

    public static void resumeJob(Scheduler scheduler, Integer jobId) {
        try {
            scheduler.resumeJob(getJobKey(jobId));
        } catch (SchedulerException e) {
            throw new CodeStackException(e);
        }
    }

    public static void deleteScheduleJob(Scheduler scheduler, Integer jobId) {
        try {
            scheduler.deleteJob(getJobKey(jobId));
        } catch (SchedulerException e) {
            throw new CodeStackException(e);
        }
    }
}
