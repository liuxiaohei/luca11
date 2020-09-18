package org.ld.mq;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * @author ld
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {org.ld.mq.TestServer.class})
public class MiscTest {

    @Test
    public void demo() {
    }

    @Resource
    private MqClient mqClient;

    @Test
    public void sendTopicMessage() {
        String messageId = "aaa";
        String messageData = "message: M A N ";
        String createTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        Map<String, Object> manMap = new HashMap<>();
        manMap.put("messageId", messageId);
        manMap.put("messageData", messageData);
        manMap.put("createTime", createTime);
        mqClient.send("topic", manMap);
    }

}
