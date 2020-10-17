package org.ld.actors;

import akka.actor.AbstractLoggingActor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;

@Slf4j
@Component("counter")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class CounterActor extends AbstractLoggingActor {

    private int counter = 0;

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(String.class, e -> {
                    counter++;
//                    var client = HttpClient.newHttpClient();
//                    HttpRequest request = null;
//                    request = HttpRequest.newBuilder()
//                            .uri(new URI("https://www.baidu.com/"))
//                            .GET()
//                            .build();
//                    var response = client.send(request, HttpResponse.BodyHandlers.ofString());
//                    log.info(response.statusCode() + "");
//                    log.info(response.body() + "");
                    log().info("Increased counter " + counter);
                })
                .matchAny(e -> log().info("接收到消息:{}", e))
                .build();
//        context().system().terminate();
    }
}