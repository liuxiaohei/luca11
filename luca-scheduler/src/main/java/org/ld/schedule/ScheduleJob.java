package org.ld.schedule;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(name = "任务")
public class ScheduleJob {
    private Integer id;

    private String name;

    private String serviceName;

    private String beanName;

    private String methodName;

    private String params;

    private String cronExpression;

    private Integer status;

    private Boolean aSync = false;

}
