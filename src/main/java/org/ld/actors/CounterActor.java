package org.ld.actors;

import akka.actor.AbstractLoggingActor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component("counter")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class CounterActor extends AbstractLoggingActor {

    private int counter = 0;

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(String.class, e -> {
                    counter++;
                    log().info("Increased counter " + counter);
                })
                .matchAny(e -> log().info("接收到消息:{}", e))
                .build();
//        context().system().terminate();
    }
}