package org.ld.engine;

import org.ld.exception.CodeStackException;
import org.ld.pojo.Job;
import org.ld.service.RpcService;
import org.ld.utils.JobUtils;
import org.ld.utils.JsonUtil;
import org.ld.utils.SpringBeanFactory;
import org.ld.utils.ZLogger;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;

public class JobRunnable extends QuartzJobBean {
    private final Logger logger = ZLogger.newInstance();
    private Object target;
    private Method method;
    private String params;

    @Override
    protected void executeInternal(JobExecutionContext context) {
        Job job = (Job) context.getMergedJobDataMap().get(JobUtils.JOB_PARAM_KEY);
        try {
            logger.info("任务准备执行，任务ID：" + job.getId());
            target = SpringBeanFactory.getBean(RpcService.class);
            params = JsonUtil.obj2Json(job);
            method = target.getClass().getDeclaredMethod("sendClient", String.class);
            CompletableFuture.runAsync(() -> {
                try {
                    ReflectionUtils.makeAccessible(method);
                    method.invoke(target, params);
                } catch (Exception e) {
                    throw new CodeStackException(e);
                }
            });
            logger.info("任务执行完毕，任务ID：{}", job.getId());
        } catch (Exception e) {
            logger.error("任务执行失败，任务ID:{}", job.getId(), e);
        }
    }

}
