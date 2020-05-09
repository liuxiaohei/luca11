package org.ld.config;

import akka.actor.Actor;
import akka.actor.IndirectActorProducer;
import org.springframework.context.ApplicationContext;

public class DIProducer implements IndirectActorProducer {
    private final ApplicationContext context;
    private final String beanName;

    public DIProducer(ApplicationContext context, String beanName) {
        this.context = context;
        this.beanName = beanName;
    }

    @Override
    public Actor produce() {
        return (Actor) context.getBean(beanName);
    }

    @Override
    public Class<? extends Actor> actorClass() {
        return (Class<? extends Actor>) context.getType(beanName);
    }
}