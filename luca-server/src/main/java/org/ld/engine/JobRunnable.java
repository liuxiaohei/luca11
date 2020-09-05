package org.ld.engine;

import lombok.SneakyThrows;
import org.ld.exception.CodeStackException;
import org.ld.pojo.Job;
import org.ld.service.RpcService;
import org.ld.utils.JobUtils;
import org.ld.utils.SpringBeanFactory;
import org.ld.utils.ZLogger;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.PersistJobDataAfterExecution;
import org.slf4j.Logger;
import org.springframework.scheduling.quartz.QuartzJobBean;

@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class JobRunnable extends QuartzJobBean {

    private final Logger logger = ZLogger.newInstance();

    @SneakyThrows
    @Override
    protected void executeInternal(JobExecutionContext context) {
        Job job = (Job) context.getMergedJobDataMap().get(JobUtils.JOB_PARAM_KEY);
        try {
            logger.info("任务准备执行，任务ID：" + job.getId());
            SpringBeanFactory.getBean(RpcService.class).sendClient(job);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CodeStackException(e);
        } catch (Exception e) {
            logger.info("任务执行完毕，任务ID：{}", job.getId());
            logger.error("任务执行失败，任务ID:{}", job.getId(), e);
        }
    }

}
