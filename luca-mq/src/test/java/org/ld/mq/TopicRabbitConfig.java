package org.ld.mq;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

/**
 * https://blog.csdn.net/qq_35387940/article/details/100514134
 * 一个发布多个订阅的模式的实现方式
 * 基于一个topic 发布多个队列 然后多个消费者基于这个topic进行消费
 */
@Configuration
@Log4j2
@Getter
public class TopicRabbitConfig {

    @Value("${topic.name:topic}")
    private String topic;

    @Value("${firstQueue:firstQueue}")
    private String firstQueue;

    @Value("${secondQueue:secondQueue}")
    private String secondQueue;

    @Resource
    private RabbitConfig rabbitConfig;

    @Bean
    public Queue firstQueue() {
        return new Queue(firstQueue);
    }

    @Bean
    public Queue secondQueue() {
        return new Queue(secondQueue);
    }

    @Bean
    Binding bindingExchangeMessage() {
        return BindingBuilder.bind(firstQueue()).to(rabbitConfig.getExchange()).with(topic);
    }

    @Bean
    Binding bindingExchangeMessage2() {
        return BindingBuilder.bind(secondQueue()).to(rabbitConfig.getExchange()).with(topic);
    }

}
