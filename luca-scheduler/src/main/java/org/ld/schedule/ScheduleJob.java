package org.ld.schedule;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleJob implements Serializable {

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
