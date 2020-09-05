package org.ld.beans;

import java.util.Date;

public class JobBean {
    public Integer id;
    //任务名称
    public String name;
    //服务名称
    public String serviceName;
    //bean
    public String beanName;
    //方法名
    public String methodName;
    //cron表达式
    public String cronExpression;
    //参数
    public String params;
    //状态
    public Integer status;
    //地址
    public String host;
    //端口
    public Integer port;
    //创建时间
    public Date createTime;

}
