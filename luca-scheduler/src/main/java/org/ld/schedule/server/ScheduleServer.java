package org.ld.schedule.server;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.ld.utils.SpringBeanFactory;
import org.ld.utils.StringUtil;
import org.ld.schedule.ScheduleJob;
import org.springframework.util.ReflectionUtils;

// todo 扫描自定义注解 发送数据到Schedule 服务
@Slf4j
public class ScheduleServer {

    /**
     * 通过反射可以调用Bean容器中的任何一个方法
     */
    @SneakyThrows
    public Object receive(ScheduleJob job) {
        final var target = SpringBeanFactory.getBean(job.getBeanName()).getClass();
        final var params = job.getParams();
        final var hasParams = StringUtil.isBlank(params);
        final var method = hasParams ? target.getDeclaredMethod(job.getMethodName()) : target.getDeclaredMethod(job.getMethodName(), String.class);
        ReflectionUtils.makeAccessible(method);
        return hasParams ? method.invoke(target) : method.invoke(target, params);
    }
}
