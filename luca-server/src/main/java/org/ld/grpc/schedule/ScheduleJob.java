package org.ld.grpc.schedule;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleJob implements Serializable {

    /**
     * 任务调度参数key
     */
    public static final String JOB_PARAM_KEY = "JOB_PARAM_KEY";

    private static final long serialVersionUID = 1L;

    private Integer id;

    private String host;

    private Integer port;

    private String beanName;

    private String methodName;

    private String name;

    private String cronExpression;

    private String params;

    private Integer status;

    private Integer deleted;

    private Date createTime;

    private String serviceName;

}
