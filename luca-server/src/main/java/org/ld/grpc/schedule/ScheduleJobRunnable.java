package org.ld.grpc.schedule;

import org.ld.exception.CodeStackException;
import org.ld.utils.SpringBeanFactory;
import org.ld.utils.StringUtil;
import org.ld.utils.ZLogger;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;

public class ScheduleJobRunnable extends QuartzJobBean {
    private final Logger logger = ZLogger.newInstance();
    private Object target;
    private Method method;
    private String params;

    @Override
    protected void executeInternal(JobExecutionContext context) {
        ScheduleJob scheduleJob = (ScheduleJob) context.getMergedJobDataMap().get(ScheduleJob.JOB_PARAM_KEY);
        try {
            logger.debug("任务准备执行，任务ID：" + scheduleJob.getId());
            target = SpringBeanFactory.getBean(scheduleJob.getBeanName());
            params = scheduleJob.getParams();
            if (StringUtil.isNotBlank(params)) {
                method = target.getClass().getDeclaredMethod(scheduleJob.getMethodName(), String.class);
            } else {
                method = target.getClass().getDeclaredMethod(scheduleJob.getMethodName());
            }
            CompletableFuture.runAsync(() -> {
                try {
                    ReflectionUtils.makeAccessible(method);
                    if (StringUtil.isNotBlank(params)) {
                        method.invoke(target, params);
                    } else {
                        method.invoke(target);
                    }
                } catch (Exception e) {
                    throw new CodeStackException(e);
                }
            });
            logger.debug("任务执行完毕，任务ID：{}", scheduleJob.getId());
        } catch (Exception e) {
            logger.error("任务执行失败，任务ID:{}", scheduleJob.getId(), e);
        }
    }

}
