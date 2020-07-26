package org.ld.actors;

import akka.actor.AbstractActor;
import org.ld.utils.ZLogger;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component("counter")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class CounterActor extends AbstractActor {

    private int counter = 0;

    private static final org.slf4j.Logger LOG = ZLogger.newInstance();

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Object.class, e -> {
                    counter++;
                    LOG.info("Increased counter " + counter);
                })
                .build();
    }
}