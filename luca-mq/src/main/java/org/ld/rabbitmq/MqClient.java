package org.ld.rabbitmq;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

/**
 * mq发送终端
 */
@Configuration
public class MqClient {

    @Resource
    private RabbitTemplate rabbitTemplate;

    @Resource
    private RabbitConfig rabbitConfig;

    /**
     * 向mq发送一个topic的消息
     */
    public void send(String topic, Object messageObject) {
        rabbitTemplate.convertAndSend(rabbitConfig.getDefaultExchange(), topic, messageObject);
    }
}