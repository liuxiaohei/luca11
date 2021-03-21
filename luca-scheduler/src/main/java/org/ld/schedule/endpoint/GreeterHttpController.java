package org.ld.schedule.endpoint;

import org.ld.schedule.ScheduleJob;
import org.ld.schedule.server.ScheduleServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class GreeterHttpController {

    @Autowired
    ScheduleServer scheduleServer;

    @PostMapping("/job/message")
    public Mono<Object> messageForHttp(@RequestBody ScheduleJob job) {
        return Mono.fromSupplier(() -> scheduleServer.receive(job));
    }
}
