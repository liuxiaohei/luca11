package org.ld.schedule.client;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.ld.schedule.ScheduleJob;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;

/**
 * Schedule Service
 */
@Service
@Slf4j
public class ScheduleClient {

    @Resource
    private RestTemplate restTemplate;

    @SneakyThrows
    public Object send(ScheduleJob job, String ip, long port) {
        return restTemplate.postForObject("http://" + ip + ":" + port + "/job/message", job, Object.class);
    }
}
