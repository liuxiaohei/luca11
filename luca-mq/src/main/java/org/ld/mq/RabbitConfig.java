package org.ld.mq;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * https://blog.csdn.net/qq_35387940/article/details/100514134
 * 一个发布多个订阅的模式的实现方式
 * 基于一个topic 发布多个队列 然后多个消费者基于这个topic进行消费
 */
@Configuration
@Log4j2
@Getter
public class RabbitConfig {

    @Value("${mq.exchange:lucaExchange}")
    private String defaultExchange;

    @Bean
    public TopicExchange getExchange() {
        return new TopicExchange(defaultExchange);
    }

    /**
     * 然后是配置相关的消息确认回调函数
     */
    @Bean
    public RabbitTemplate createRabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate();
        rabbitTemplate.setConnectionFactory(connectionFactory);
        //设置开启Mandatory,才能触发回调函数,无论消息推送结果怎么样都强制调用回调函数
        rabbitTemplate.setMandatory(true);
        rabbitTemplate.setConfirmCallback(
                (correlationData,
                 ack,
                 cause
                ) -> {
                    log.info("ConfirmCallback:     " + "相关数据：" + correlationData);
                    log.info("ConfirmCallback:     " + "确认情况：" + ack);
                    log.info("ConfirmCallback:     " + "原因：" + cause);
                });

        rabbitTemplate.setReturnCallback(
                (message,
                 replyCode,
                 replyText,
                 exchange,
                 routingKey) -> {
                    log.info("ReturnCallback:     " + "消息：" + message);
                    log.info("ReturnCallback:     " + "回应码：" + replyCode);
                    log.info("ReturnCallback:     " + "回应信息：" + replyText);
                    log.info("ReturnCallback:     " + "交换机：" + exchange);
                    log.info("ReturnCallback:     " + "路由键：" + routingKey);
                });

        return rabbitTemplate;
    }
}
