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
    /**
     * 任务id
     */
    private Integer id;

    /**
     * spring bean名称
     */
    private String beanName;

    /**
     * 方法名
     */
    private String methodName;

    /**
     * 参数
     */
    private String params;

    /**
     * cron表达式
     */
    private String cronExpression;

    /**
     * 任务状态 1：暂停
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private Date createTime;

    private String serverName;

    private String host;

    private Integer port;

    private String name;

    private Integer deleted;

    private String serviceName;

}
