package org.ld.engine;

import lombok.SneakyThrows;
import org.ld.uc.UCRunnable;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.springframework.scheduling.quartz.QuartzJobBean;

public class RunnableQuartzJob extends QuartzJobBean {

    @SneakyThrows
    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) {
        JobDataMap jobDataMap = jobExecutionContext.getJobDetail().getJobDataMap();
        UCRunnable runnable = (UCRunnable)jobDataMap.get("Runnable");
        runnable.run();
    }
}
