package org.ld.actors;

import akka.actor.AbstractActor;
import org.ld.utils.LoggerUtil;
import org.slf4j.Logger;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class TestActor extends AbstractActor {

    private static final Logger LOG = LoggerUtil.newInstance();

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .matchAny(o -> LOG.info("接受到消息：" + o))
                .build();
    }
}