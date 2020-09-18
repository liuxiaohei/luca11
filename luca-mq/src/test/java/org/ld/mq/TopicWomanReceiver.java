package org.ld.mq;


import com.rabbitmq.client.Channel;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
@RabbitListener(queues = "secondQueue")
@Log4j2
public class TopicWomanReceiver {

    @RabbitHandler
    public void process(Map<String, String> testMessage, Channel channel, Message message) throws IOException {
        try {
            log.info("TopicWomanReceiver 消费者收到消息  : " + testMessage.toString());
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
        }
    }
}

