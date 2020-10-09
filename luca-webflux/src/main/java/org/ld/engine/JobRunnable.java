package org.ld.engine;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.ld.grpc.schedule.ScheduleJob;
import org.ld.service.RpcService;
import org.ld.utils.SpringBeanFactory;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.PersistJobDataAfterExecution;
import org.springframework.scheduling.quartz.QuartzJobBean;

import static org.ld.service.JobService.JOB_PARAM_KEY;

@PersistJobDataAfterExecution
@DisallowConcurrentExecution
@Slf4j
public class JobRunnable extends QuartzJobBean {

    @SneakyThrows
    @Override
    protected void executeInternal(JobExecutionContext context) {
        ScheduleJob job = (ScheduleJob) context.getMergedJobDataMap().get(JOB_PARAM_KEY);
        try {
            log.info("任务准备执行，任务ID：" + job.getId());
            SpringBeanFactory.getBean(RpcService.class).sendClient(job);
        } catch (Exception e) {
            log.error("任务执行失败，任务ID:{}", job.getId(), e);
        }
    }
}
